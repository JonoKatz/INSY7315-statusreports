using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace LeaseLogic.Services
{
    public class FirebaseAuthService
    {
        private readonly string _apiKey = "AIzaSyCiUjP0ZqdhcDCBQ4lsNM5UUhE-GAp_jOk";

        public async Task<string?> SignInWithEmailPasswordAsync(string email, string password)
        {
            email = email.Trim().ToLower();
            using var client = new HttpClient();
            var payload = new { email, password, returnSecureToken = true };
            var content = new StringContent(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");

            var response = await client.PostAsync(
                $"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={_apiKey}",
                content
            );

            var resultJson = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                Console.WriteLine("Firebase SignIn failed: " + resultJson);
                return null;
            }

            using var doc = JsonDocument.Parse(resultJson);
            return doc.RootElement.GetProperty("idToken").GetString();
        }

        public async Task<string?> SignUpWithEmailPasswordAsync(string email, string password)
        {
            email = email.Trim().ToLower();
            using var client = new HttpClient();
            var payload = new { email, password, returnSecureToken = true };
            var content = new StringContent(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");

            var response = await client.PostAsync(
                $"https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={_apiKey}",
                content
            );

            var resultJson = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                // 🔹 Log Firebase error for debugging
                Console.WriteLine("Firebase SignUp failed: " + resultJson);
                return null;
            }

            using var doc = JsonDocument.Parse(resultJson);
            return doc.RootElement.GetProperty("idToken").GetString();
        }
    }
}
