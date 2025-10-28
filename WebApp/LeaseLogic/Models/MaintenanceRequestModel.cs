using Google.Cloud.Firestore;

namespace LeaseLogic.Models
{
    [FirestoreData]
    public class MaintenanceRequestModel
    {
        [FirestoreProperty] public string Id { get; set; } = "";
        [FirestoreProperty] public string PropertyId { get; set; } = "";
        [FirestoreProperty] public string PropertyName { get; set; } = "";
        [FirestoreProperty] public string TenantId { get; set; } = "";
        [FirestoreProperty] public string TenantName { get; set; } = "";
        [FirestoreProperty] public string Description { get; set; } = "";  // mandatory
        [FirestoreProperty] public string Status { get; set; } = "Pending"; // Pending, In Progress, Completed
        [FirestoreProperty] public long Timestamp { get; set; } = System.DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        [FirestoreProperty] public long? DateFixed { get; set; } = null; // nullable
    }
}
