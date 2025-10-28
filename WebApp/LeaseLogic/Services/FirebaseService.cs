using Google.Cloud.Firestore;
using LeaseLogic.Models;
using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.IO;
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

        public async Task<bool> CreateUserAsync(UserModel user)
        {
            try
            {
                var userRef = _db.Collection("users ").Document(user.email);
                await userRef.SetAsync(user);
                return true;
            }
            catch
            {
                return false;
            }
        }

        // ✅ Retrieve a user by email
        public async Task<UserModel> GetUserByEmailAsync(string email)
        {
            var userRef = _db.Collection("users").Document(email);
            var snapshot = await userRef.GetSnapshotAsync();
            if (snapshot.Exists)
            {
                return snapshot.ConvertTo<UserModel>();
            }
            return null;
        }

        // ✅ (optional) Retrieve all users
        public async Task<List<UserModel>> GetAllUsersAsync()
        {
            var users = new List<UserModel>();
            var snapshot = await _db.Collection("users").GetSnapshotAsync();
            foreach (var doc in snapshot.Documents)
            {
                var user = doc.ConvertTo<UserModel>();
                users.Add(user);
            }
            return users;
        }

        // ---------------- Properties ----------------
        public async Task<bool> AddPropertyAsync(PropertyModel property, List<IFormFile>? imageFiles = null)
        {
            try
            {
                if (string.IsNullOrEmpty(property.Id))
                    property.Id = Guid.NewGuid().ToString();

                property.DateCreated = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
                property.Images ??= new List<byte[]>();

                // Upload images (if provided)
                if (imageFiles != null)
                {
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
            catch (Exception ex)
            {
                Console.WriteLine($"AddPropertyAsync Error: {ex.Message}");
                return false;
            }
        }

        public async Task<List<PropertyModel>> GetPropertiesAsync(bool includeDeleted = false)
        {
            var snapshot = await _db.Collection("properties").GetSnapshotAsync();

            var list = snapshot.Documents
                .Where(d => d.Exists)
                .Select(d =>
                {
                    var property = d.ConvertTo<PropertyModel>();

                    // ensure Images is not null
                    property.Images ??= new List<byte[]>();

                    return property;
                })
                .ToList();

            if (!includeDeleted)
                list = list.Where(p => p.DeletedAt == null).ToList();

            return list;
        }



        public async Task<PropertyModel?> GetPropertyByIdAsync(string id)
        {
            try
            {
                var doc = await _db.Collection("properties").Document(id).GetSnapshotAsync();
                if (doc.Exists)
                    return doc.ConvertTo<PropertyModel>();
                else
                    return null;
            }
            catch
            {
                return null;
            }
        }


        public async Task<List<MaintenanceRequestModel>> GetMaintenanceRequestsAsync(string? tenantId = null, bool? onlyCompleted = false)
        {
            var snapshot = await _db.Collection("MaintenanceRequests").GetSnapshotAsync();
            var requests = snapshot.Documents
                .Select(d => d.ConvertTo<MaintenanceRequestModel>())
                .ToList();

            // Filter by tenant if provided
            if (!string.IsNullOrEmpty(tenantId))
                requests = requests.Where(r => r.TenantId == tenantId).ToList();

            // Filter by completion if requested
            if ((bool)onlyCompleted)
                requests = requests.Where(r => r.Status == "Completed").ToList();

            return requests;
        }


        public async Task<bool> UpdatePropertyAsync(PropertyModel property, List<IFormFile>? newImages = null)
        {
            try
            {
                var docRef = _db.Collection("properties").Document(property.Id);
                var existingDoc = await docRef.GetSnapshotAsync();
                if (!existingDoc.Exists)
                    return false;

                var existingProperty = existingDoc.ConvertTo<PropertyModel>();

                // Keep existing images
                property.Images = existingProperty.Images ?? new List<byte[]>();

                // Append new images
                if (newImages != null)
                {
                    foreach (var file in newImages)
                    {
                        using var ms = new MemoryStream();
                        await file.CopyToAsync(ms);
                        property.Images.Add(ms.ToArray());
                    }
                }

                await docRef.SetAsync(property, SetOptions.Overwrite);
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"UpdatePropertyAsync Error: {ex.Message}");
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
            catch (Exception ex)
            {
                Console.WriteLine($"DeletePropertyAsync Error: {ex.Message}");
                return false;
            }
        }
    }
}
