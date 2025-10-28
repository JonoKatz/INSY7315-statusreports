using Google.Cloud.Firestore;
using LeaseLogic.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace LeaseLogic.Services
{
    public class FirebaseService
    {
        private readonly FirestoreDb _db;

        public FirebaseService()
        {
            _db = FirebaseConnector.GetFirestoreDb();
        }

        // ---------------- Users ----------------
        public async Task<UserModel?> GetUserByEmailAsync(string email)
        {
            var usersRef = _db.Collection("users");
            var query = usersRef.WhereEqualTo("email", email);
            var snapshot = await query.GetSnapshotAsync();

            foreach (var doc in snapshot.Documents)
            {
                if (doc.Exists) return doc.ConvertTo<UserModel>();
            }

            return null;
        }

        public async Task<bool> CreateUserAsync(UserModel user)
        {
            try
            {
                var docRef = _db.Collection("users").Document(user.email);
                await docRef.SetAsync(user);
                return true;
            }
            catch
            {
                return false;
            }
        }

        // ---------------- Properties ----------------
        public async Task<bool> AddPropertyAsync(PropertyModel property, List<IFormFile>? imageFiles = null)
        {
            try
            {
                if (string.IsNullOrEmpty(property.Id))
                    property.Id = Guid.NewGuid().ToString();

                property.DateCreated = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

                // Convert uploaded images to byte arrays
                if (imageFiles != null)
                {
                    property.Images.Clear();
                    foreach (var file in imageFiles)
                    {
                        using var ms = new MemoryStream();
                        await file.CopyToAsync(ms);
                        property.Images.Add(ms.ToArray());
                    }
                }

                var docRef = _db.Collection("properties").Document(property.Id);
                await docRef.SetAsync(property);
                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<List<PropertyModel>> GetPropertiesAsync(bool includeDeleted = false)
        {
            var snapshot = await _db.Collection("properties").GetSnapshotAsync();
            var list = snapshot.Documents
                .Where(d => d.Exists)
                .Select(d => d.ConvertTo<PropertyModel>())
                .ToList();

            if (!includeDeleted)
                list = list.Where(p => p.DeletedAt == null).ToList();

            return list;
        }

        public async Task<PropertyModel?> GetPropertyByIdAsync(string id)
        {
            var doc = await _db.Collection("properties").Document(id).GetSnapshotAsync();
            return doc.Exists ? doc.ConvertTo<PropertyModel>() : null;
        }

        public async Task<bool> UpdatePropertyAsync(PropertyModel property)
        {
            try
            {
                var docRef = _db.Collection("properties").Document(property.Id);
                await docRef.SetAsync(property, SetOptions.Overwrite);
                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<bool> DeletePropertyAsync(string id, bool softDelete = true)
        {
            try
            {
                var docRef = _db.Collection("properties").Document(id);

                if (softDelete)
                {
                    await docRef.UpdateAsync(new Dictionary<string, object>
                    {
                        { "DeletedAt", DateTimeOffset.UtcNow.ToUnixTimeMilliseconds() },
                        { "Status", "Deleted" }
                    });
                }
                else
                {
                    await docRef.DeleteAsync();
                }

                return true;
            }
            catch
            {
                return false;
            }
        }

        // ---------------- Maintenance ----------------
        // ---------------- Maintenance ----------------
        public async Task<List<MaintenanceRequestModel>> GetMaintenanceRequestsAsync(
            string? tenantId = null, bool? onlyCompleted = null)
        {
            var snapshot = await _db.Collection("maintenance").GetSnapshotAsync();
            var list = snapshot.Documents
                .Where(d => d.Exists)
                .Select(d => d.ConvertTo<MaintenanceRequestModel>())
                .ToList();

            // Filter by tenant if provided
            if (!string.IsNullOrEmpty(tenantId))
                list = list.Where(r => r.TenantId == tenantId).ToList();

            // Filter by completed status if requested
            if (onlyCompleted.HasValue)
            {
                if (onlyCompleted.Value)
                    list = list.Where(r => r.Status == "Completed").ToList();
                else
                    list = list.Where(r => r.Status == "Pending" || r.Status == "In Progress").ToList();
            }

            return list.OrderByDescending(r => r.Timestamp).ToList();
        }

    }
}
