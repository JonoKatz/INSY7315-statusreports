using Google.Cloud.Firestore;

namespace LeaseLogic.Models
{
    [FirestoreData]
    public class MaintenanceRequestModel
    {
        [FirestoreProperty] public string Id { get; set; }
        [FirestoreProperty] public string Title { get; set; }
        [FirestoreProperty] public string Description { get; set; }
        [FirestoreProperty] public string Status { get; set; }
        [FirestoreProperty] public string PropertyName { get; set; }
        [FirestoreProperty] public string TenantName { get; set; }
        [FirestoreProperty] public long Timestamp { get; set; }
    }
}
