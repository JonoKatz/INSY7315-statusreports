using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;

namespace LeaseLogic
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            // Add services to the container.
            builder.Services.AddControllersWithViews();

            // ? Add session services
            builder.Services.AddDistributedMemoryCache(); // required by session
            builder.Services.AddSession(options =>
            {
                options.IdleTimeout = TimeSpan.FromMinutes(30);
                options.Cookie.HttpOnly = true;
                options.Cookie.IsEssential = true;
            });

            var app = builder.Build();

            // Configure the HTTP request pipeline.
            if (!app.Environment.IsDevelopment())
            {
                app.UseExceptionHandler("/Home/Error");
                app.UseHsts();
            }

            app.UseHttpsRedirection();
            app.UseStaticFiles();

            app.UseRouting();

            // ? Enable session AFTER routing
            app.UseSession();

            app.UseAuthorization();

            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Account}/{action=Login}/{id?}");

            // Initialize Firebase Admin SDK
            string path = Path.Combine(AppContext.BaseDirectory, "serviceAccountKey.json");
            if (FirebaseApp.DefaultInstance == null)
            {
                FirebaseApp.Create(new AppOptions()
                {
                    Credential = GoogleCredential.FromFile(path)
                });
            }

            // Connect to Firestore
            FirestoreDb db = new FirestoreDbBuilder
            {
                ProjectId = "leaselogic-e0fd5",
                Credential = GoogleCredential.FromFile(path)
            }.Build();

            Console.WriteLine("Connected to Firestore project: " + db.ProjectId);

            app.Run();
        }
    }
}
