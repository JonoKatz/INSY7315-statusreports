# LeaseLogic – Property Management System

LeaseLogic is a cross-platform property management system that simplifies communication between landlords and tenants.  
It includes:

- A **mobile application (Android, Kotlin)** for on-the-go property and maintenance management.  
- A **web application (ASP.NET Core MVC)** for centralized dashboard control and reporting.  

Both applications use **Firebase Firestore** as the backend to synchronize property listings, tenant data, and maintenance requests in real time.

---

# LeaseLogic Android Application (Mobile Version)

The LeaseLogic Mobile App is built in Kotlin using Firebase Firestore as its backend.  
It provides separate dashboards for landlords and tenants with real-time synchronization, intuitive UI design, and role-specific functionality.

## Landlord Features

### Dashboard
- View total properties, occupied properties, total rent, and maintenance statistics.
- Charts for monthly income and maintenance distribution.
- Real-time updates using Firestore listeners.

### Property Management
- Add, edit, view, and delete properties.
- Upload multiple property images (Firebase Storage).
- View detailed property information with image gallery.
- Filter and sort properties by price, date, or status.

### Tenant Management
- View all tenants linked to your properties.
- Approve or reject tenant applications.
- Filter tenants by status (Pending, Active, Rejected).
- View tenant details including profile picture and lease duration.

### Maintenance Management
- View all maintenance requests across your properties.
- Filter by status (Pending, In Progress, Resolved).
- Update request statuses directly from the app.
- View property, tenant, and issue details.

### Reports
- Generate monthly income and maintenance reports.
- Bar and Pie charts update dynamically from Firestore.
- Refresh button with last updated timestamp.

## Tenant Features
- Submit maintenance requests (Low, Medium, High priority).
- Track request status in real time.
- View property information and landlord details.
- Manage personal profile with optional photo upload.

## Technologies Used (Mobile)
- Kotlin (Android Studio)
- Firebase Firestore
- Firebase Authentication
- Firebase Storage
- Picasso (image loading)
- MPAndroidChart (data visualization)
- Material Design 3 Components
- ViewBinding and RecyclerView

## Setup Instructions (Mobile)

### Prerequisites
- Android Studio (latest version)
- Firebase project with Firestore and Authentication enabled
- Emulator or Android device (Android 11+ recommended)

### Steps
1. Clone the repository:

git clone [https://github.com/JonoKatz/INSY7315-statusreports](https://github.com/JonoKatz/INSY7315-statusreports)

cd LeaseLogic


2. Open the LeaseLogic project in Android Studio.
3. Add your `google-services.json` file inside `/app/`.
4. Enable Firestore, Storage, and Authentication in Firebase.
5. Build and run:
6. Run on an emulator or device (Pixel 9 Pro API 34 recommended).

---

# LeaseLogic Web Application

The web version complements the mobile app, providing extended reporting and administrative tools for landlords and property managers.

## Features

### Dashboard
- View total properties, occupied properties, total rent, and pending maintenance requests.
- Charts for property overview and income trends.

### Property Management
- Add, view, edit, and delete properties.
- Upload property images.
- View property details.

### Tenant Management
- List tenants.
- Filter tenants by status (Active / Inactive).
- View tenant contact information and profile picture.

### Maintenance Requests
- List maintenance requests.
- Filter by status: Pending, In Progress, Completed.
- View details such as property, tenant, description, reported date, and fixed date.

### Reports
- Generate reports for properties, tenants, and maintenance statistics.

## Technologies Used (Web)
- ASP.NET Core MVC (.NET 8)
- Firebase Firestore (Google.Cloud.Firestore)
- Razor Pages for views
- Bootstrap 5 and custom CSS
- Azure Functions (optional backend automation)
- Android app integration (shared Firestore backend)

## Setup Instructions (Web)

### Prerequisites
- .NET 8 SDK
- Visual Studio 2022 or later
- Firebase project with Firestore enabled
- Google Cloud SDK credentials JSON file

### Steps
1. Clone the repository:
   
git clone [https://github.com/JonoKatz/INSY7315-statusreports](https://github.com/JonoKatz/INSY7315-statusreports)

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
4. Build the project:
5. Run the project:


Access the application at `https://localhost:5001/` or `http://localhost:5000/`.

---

# Project Architecture

```
LeaseLogic/
│
├── LeaseLogic.Web/ # ASP.NET Core MVC project
│ ├── Controllers/
│ ├── Views/
│ ├── Models/
│ └── FirebaseConnector.cs
│
├── LeaseLogic.Mobile/ # Android project (Kotlin)
│ ├── com.vcsd.leaselogic/
│ │ ├── landlord/
│ │ ├── tenant/
│ │ ├── adapters/
│ │ ├── models/
│ │ ├── utils/
│ │ └── ui/
│
└── README.md
```


---

# Team Contributions

| Member | Role | Contribution |
|---------|------|--------------|
| Jonathan Katz | Android Developer | Designed and developed the LeaseLogic Mobile App (Kotlin, Firebase Firestore, Material Design 3, MPAndroidChart, Firebase Auth). |
| Tyler Bolle | Web Developer | Built the LeaseLogic Web App using ASP.NET Core MVC and integrated shared Firestore backend. |
| Lishen Nadeson | System Analyst  | Documents weekly progress. |


---

# Repository Information
- Repository: [https://github.com/JonoKatz/INSY7315-statusreports](https://github.com/JonoKatz/INSY7315-statusreports)
- License: MIT
- Languages: Kotlin, C#, JavaScript, HTML, CSS
- Database: Firebase Firestore

---

# Conclusion

LeaseLogic bridges the gap between landlords and tenants through a synchronized, multi-platform ecosystem.  
The mobile app provides quick property, maintenance, and tenant management on the go, while the web app offers complete administrative control and visual reporting.  

Together, they deliver a modern, scalable, and user-friendly property management system.




