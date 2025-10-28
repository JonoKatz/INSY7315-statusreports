using Google.Cloud.Firestore;
using System.Collections.Generic;

namespace LeaseLogic.Models
{
    [FirestoreData]
    public class PropertyModel
    {
        [FirestoreProperty] public string Id { get; set; } = "";
        [FirestoreProperty] public string LandlordId { get; set; } = "";
        [FirestoreProperty] public string Name { get; set; } = "";
        [FirestoreProperty] public double Price { get; set; } = 0.0;
        [FirestoreProperty] public string Size { get; set; } = "";
        [FirestoreProperty] public string Location { get; set; } = "";
        [FirestoreProperty] public string? Description { get; set; } = "";
        [FirestoreProperty] public string? ImageUrl { get; set; } = "";
        [FirestoreProperty] public List<string> ImageUrls { get; set; } = new List<string>();
        [FirestoreProperty] public string Status { get; set; } = "Available";
        [FirestoreProperty] public long? DeletedAt { get; set; } = null;
        [FirestoreProperty] public bool IsRented { get; set; } = false;
        [FirestoreProperty] public long DateCreated { get; set; } = System.DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        [FirestoreProperty] public int Units { get; set; } = 0;
        [FirestoreProperty] public double Occupancy { get; set; } = 0;
    }
}
