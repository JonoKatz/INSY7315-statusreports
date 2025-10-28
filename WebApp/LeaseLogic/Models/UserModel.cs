namespace LeaseLogic.Models
{
    using Google.Cloud.Firestore;

    [FirestoreData]
    public class UserModel
    {
        [FirestoreProperty] public string email { get; set; }
        [FirestoreProperty] public string name { get; set; }
        [FirestoreProperty] public string role { get; set; } // Landlord/Tenant
    }
}
