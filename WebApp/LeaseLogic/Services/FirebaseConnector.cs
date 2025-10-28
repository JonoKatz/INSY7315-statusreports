using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using System;
using System.IO;

namespace LeaseLogic.Services
{
    public static class FirebaseConnector
    {
        private static FirestoreDb? _db;

        public static FirestoreDb GetFirestoreDb()
        {
            if (_db != null) return _db;

            string path = Path.Combine(AppContext.BaseDirectory, "serviceAccountKey.json");
            if (!File.Exists(path))
                throw new FileNotFoundException("serviceAccountKey.json not found at: " + path);

            var credential = GoogleCredential.FromFile(path);
            _db = new FirestoreDbBuilder
            {
                ProjectId = "leaselogic-e0fd5",
                Credential = credential
            }.Build();

            Console.WriteLine($"✅ Firestore connected to project: {_db.ProjectId}");
            return _db;
        }
    }
}
