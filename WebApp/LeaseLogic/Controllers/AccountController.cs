using LeaseLogic.Models;
using LeaseLogic.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using System.Threading.Tasks;

namespace LeaseLogic.Controllers
{
    public class AccountController : Controller
    {
        private readonly FirebaseService _firebaseService = new FirebaseService();
        private readonly FirebaseAuthService _authService = new FirebaseAuthService();

        // =========================
        // REGISTER
        // =========================
        [HttpGet]
        public IActionResult Register() => View();

        [HttpPost]
        public async Task<IActionResult> Register(string name, string email, string password, string role)
        {
            if (string.IsNullOrEmpty(name) || string.IsNullOrEmpty(email)
                || string.IsNullOrEmpty(password) || string.IsNullOrEmpty(role))
            {
                ViewBag.Error = "Please fill in all fields";
                return View();
            }

            email = email.Trim().ToLower();

            // Create user in Firebase Auth
            var idToken = await _authService.SignUpWithEmailPasswordAsync(email, password);
            if (idToken == null)
            {
                ViewBag.Error = "Registration failed: email may already exist or password is too weak";
                return View();
            }

            // Save user to Firestore
            var userModel = new UserModel
            {
                email = email,
                name = name,
                role = role
            };

            var created = await _firebaseService.CreateUserAsync(userModel);
            if (!created)
            {
                ViewBag.Error = "Failed to save user data in Firestore";
                return View();
            }

            // Auto-login
            HttpContext.Session.SetString("UserEmail", userModel.email);
            HttpContext.Session.SetString("UserName", userModel.name);
            HttpContext.Session.SetString("FirebaseIdToken", idToken);

            return RedirectToAction("Index", "Dashboard");
        }

        // =========================
        // LOGIN
        // =========================
        [HttpGet]
        public IActionResult Login() => View();

        [HttpPost]
        public async Task<IActionResult> Login(string email, string password)
        {
            if (string.IsNullOrEmpty(email) || string.IsNullOrEmpty(password))
            {
                ViewBag.Error = "Please enter both email and password";
                return View();
            }

            email = email.Trim().ToLower();

            // Authenticate with Firebase Auth
            var idToken = await _authService.SignInWithEmailPasswordAsync(email, password);
            if (idToken == null)
            {
                ViewBag.Error = "Incorrect email or password";
                return View();
            }

            // Fetch user metadata from Firestore
            var user = await _firebaseService.GetUserByEmailAsync(email);
            if (user == null)
            {
                ViewBag.Error = "User not found in Firestore";
                return View();
            }

            // Set session
            HttpContext.Session.SetString("UserEmail", user.email);
            HttpContext.Session.SetString("UserName", user.name);
            HttpContext.Session.SetString("FirebaseIdToken", idToken);

            return RedirectToAction("Dashboard", "Property");
        }

        // =========================
        // LOGOUT
        // =========================
        public IActionResult Logout()
        {
            HttpContext.Session.Clear();
            return RedirectToAction("Login");
        }
    }
}
