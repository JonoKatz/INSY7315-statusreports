# LeaseLogic Web Application

LeaseLogic is an ASP.NET Core MVC web application for managing rental properties, tenants, and maintenance requests. It uses Firebase Firestore as a backend and provides dashboards, tenant management, property management, and maintenance tracking.

# Features

## Dashboard
- View total properties, occupied properties, total rent, and pending maintenance requests.
- Charts for property overview and income trends.

## Property Management
- Add, view, edit, and delete properties.
- Upload property images.
- View property details.

## Tenant Management
- List tenants.
- Filter tenants by status (Active / Inactive).
- View tenant contact information and profile picture.

## Maintenance Requests
- List maintenance requests.
- Filter by status: Pending, In Progress, Completed.
- View details such as property, tenant, description, reported date, and fixed date.

## Reports
- Generate reports for properties, tenants, and maintenance statistics.

# Technologies Used
- ASP.NET Core MVC (.NET 8)
- Firebase Firestore (Google.Cloud.Firestore)
- Razor Pages for views
- Bootstrap 5 and custom CSS
- Azure Functions (optional backend automation)
- Android app integration (Kotlin module)

# Setup Instructions

## Prerequisites
- .NET 8 SDK
- Visual Studio 2022 or later
- Firebase project with Firestore enabled
- Google Cloud SDK credentials JSON file

## Steps
1. Clone the repository:

git clone https://github.com/yourusername/LeaseLogic.git


cd LeaseLogic


2. Configure Firebase:
- Place your Firebase credentials JSON file in the project.
- Update `FirebaseConnector.cs` with the credentials path:
  ```
  var builder = new FirestoreDbBuilder
  {
      ProjectId = "your-project-id",
      Credential = GoogleCredential.FromFile("path/to/credentials.json")
  };
  ```

3. Restore NuGet packages:

dotnet restore


4. Build the project:

dotnet build


5. Run the project:

dotnet run

Access the application at `https://localhost:5001/` or `http://localhost:5000/`.
