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





Overview

LeaseLogic is a cross-platform property management system that simplifies communication between landlords and tenants.

It includes:

Mobile Application (Android – Kotlin) for property and maintenance management on the go.

Web Application (ASP.NET Core MVC) for centralized dashboards and reporting.

Both applications share a Firebase Firestore backend, ensuring real-time synchronization of properties, tenants, and maintenance data.

LeaseLogic Android Application (Mobile Version)

The mobile app is built in Kotlin (Android Studio) using Firebase Firestore as the backend.
It provides role-based dashboards, real-time synchronization, and a clean Material Design 3 interface.

Landlord Features
Dashboard

Displays total properties, occupied properties, total rent, and maintenance statistics

Interactive charts for income and maintenance trends

Real-time Firestore synchronization

Property Management

Add, edit, view, and delete properties

Upload multiple images (Firebase Storage)

Filter and sort properties by price, date, or status

View detailed property information with image gallery

Tenant Management

View all tenants linked to your properties

Approve or reject applications

Filter tenants by status (Pending / Active / Rejected)

View tenant details including property and lease duration

Maintenance Management

View all maintenance requests

Filter by status (Pending / In Progress / Resolved)

Update request statuses

View detailed request and tenant info

Reports

Generate monthly income and maintenance reports

Includes Bar and Pie charts

Data auto-refreshes from Firestore with timestamps

Tenant Features

Submit maintenance requests (Low / Medium / High priority)

Track request progress and completion

View property information and landlord details

Update profile with optional photo upload

Technologies Used (Mobile)

Kotlin (Android Studio)

Firebase Firestore

Firebase Authentication

Firebase Storage

Picasso (image loading)

MPAndroidChart (data visualization)

Material Design 3 Components

ViewBinding + RecyclerView

LeaseLogic Web Application (ASP.NET Core MVC)

The web version complements the mobile app, offering administrative dashboards, extended analytics, and report generation for property managers.

Features
Dashboard

Displays total properties, occupied units, rent totals, and maintenance stats

Charts for property overview and income trends

Property Management

Add, view, edit, and delete properties

Upload property images and view details

Tenant Management

List tenants by Active / Inactive status

View tenant contact info and profile picture

Maintenance Requests

View and manage all maintenance requests

Filter by Pending / In Progress / Completed

View related property and tenant details

Reports

Generate and export reports for:

Properties

Tenants

Maintenance Trends

Technologies Used (Web)

ASP.NET Core MVC (.NET 8)

Firebase Firestore (Google.Cloud.Firestore)

Razor Pages

Bootstrap 5 + Custom CSS

Firebase Authentication

(Optional) Azure Functions for backend automation

Shared Firestore backend with Android app

Setup Instructions
Mobile (Android)
Prerequisites

Android Studio (latest)

Firebase Project with Firestore & Authentication enabled

Emulator or Android device (Android 11+ recommended)

Steps
# Clone the repository
git clone https://github.com/yourusername/LeaseLogic.git
cd LeaseLogic


Open the project in Android Studio

Add your google-services.json inside /app/

Ensure Firestore, Storage, and Authentication are enabled in Firebase

Sync Gradle and build:

./gradlew build


Run the app on an emulator or device (Pixel 9 Pro API 34 recommended)

Web (ASP.NET Core)
Prerequisites

.NET 8 SDK

Visual Studio 2022 or newer

Firebase Project (Firestore enabled)

Google Cloud credentials JSON

Steps

Configure Firebase credentials in:

var builder = new FirestoreDbBuilder
{
    ProjectId = "your-project-id",
    Credential = GoogleCredential.FromFile("path/to/credentials.json")
};


Restore dependencies:

dotnet restore


Build and run:

dotnet run


Access the app at:

https://localhost:5001

Project Architecture
LeaseLogic/
│
├── LeaseLogic.Web/                # ASP.NET Core MVC project
│   ├── Controllers/
│   ├── Views/
│   ├── Models/
│   └── FirebaseConnector.cs
│
├── LeaseLogic.Mobile/             # Android project (Kotlin)
│   ├── com.vcsd.leaselogic/
│   │   ├── landlord/              # Landlord-specific screens
│   │   ├── tenant/                # Tenant-specific screens
│   │   ├── adapters/              # RecyclerView adapters
│   │   ├── models/                # Data models
│   │   ├── utils/                 # Helper utilities
│   │   └── ui/                    # Shared UI components
│
└── README.md                      # Unified documentation

Team Contributions
Member	Role	Contribution
Jonathan Katz (ST10252936)	Android Developer	Designed and developed the LeaseLogic Mobile App (Kotlin, Firebase Firestore, Material Design 3, MPAndroidChart, Firebase Auth).
[Teammate Name]	Web Developer	Built the LeaseLogic Web App using ASP.NET Core MVC and integrated shared Firestore backend.
Repository Information

Repository: https://github.com/yourusername/LeaseLogic

License: MIT

Languages: Kotlin, C#, JavaScript, HTML, CSS

Database: Firebase Firestore

Conclusion

LeaseLogic bridges the gap between landlords and tenants through a unified, real-time ecosystem.
The mobile app provides efficient property and maintenance management, while the web app offers powerful administration and reporting tools.

Together, they form a modern, scalable, and user-friendly platform for seamless property management.
