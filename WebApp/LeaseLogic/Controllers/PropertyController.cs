using LeaseLogic.Models;
using LeaseLogic.Services;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace LeaseLogic.Controllers
{
    public class PropertyController : Controller
    {
        private readonly FirebaseService _firebaseService = new FirebaseService();

        // ---------------- PROPERTY LIST ----------------
        public async Task<IActionResult> Index()
        {
            var properties = await _firebaseService.GetPropertiesAsync();
            return View(properties);
        }

        // ---------------- ADD PROPERTY ----------------
        [HttpGet]
        public IActionResult Add()
        {
            return View(new PropertyModel());
        }

        [HttpPost]
        public async Task<IActionResult> Add(PropertyModel property, string? imageUrlsInput)
        {
            if (!ModelState.IsValid)
                return View(property);

            property.Id = Guid.NewGuid().ToString();

            // Handle optional image URL
            if (!string.IsNullOrWhiteSpace(imageUrlsInput))
            {
                // If user enters multiple URLs, pick the first one
                property.ImageUrl = imageUrlsInput.Split(',')[0].Trim();
            }

            await _firebaseService.AddPropertyAsync(property);
            return RedirectToAction("Index");
        }


        // ---------------- EDIT PROPERTY ----------------
        [HttpGet]
        public async Task<IActionResult> Edit(string id)
        {
            var property = await _firebaseService.GetPropertyByIdAsync(id);
            if (property == null) return NotFound();

            return View(property);
        }

        [HttpPost]
        public async Task<IActionResult> Edit(PropertyModel property, string? imageUrlsInput)
        {
            if (!ModelState.IsValid)
                return View(property);

            // Handle ImageUrls
            property.ImageUrls = !string.IsNullOrWhiteSpace(imageUrlsInput)
                ? imageUrlsInput.Split(',', StringSplitOptions.RemoveEmptyEntries)
                                .Select(url => url.Trim())
                                .ToList()
                : new List<string>();

            // Handle Description
            property.Description ??= ""; // set to empty string if null

            await _firebaseService.UpdatePropertyAsync(property);
            return RedirectToAction("Index");
        }

        // ---------------- DELETE PROPERTY ----------------
        [HttpPost]
        public async Task<IActionResult> Delete(string id)
        {
            await _firebaseService.DeletePropertyAsync(id, softDelete: false);
            return RedirectToAction("Index");
        }

        [HttpGet]
        public async Task<IActionResult> View(string id)
        {
            var property = await _firebaseService.GetPropertyByIdAsync(id);
            if (property == null) return NotFound();
            return View(property); // returns Views/Property/View.cshtml
        }

        public async Task<IActionResult> Dashboard()
        {
            var properties = await _firebaseService.GetPropertiesAsync();

            if (!properties.Any())
            {
                ViewBag.OccupancyPercentage = 0;
                ViewBag.TotalRent = 0;
            }
            else
            {
                var totalUnits = properties.Sum(p => p.Units);
                var occupiedUnits = properties.Sum(p => (int)(p.Units * (p.Occupancy / 100)));
                ViewBag.OccupancyPercentage = totalUnits == 0 ? 0 : (occupiedUnits * 100) / totalUnits;

                ViewBag.TotalRent = properties.Sum(p => p.Price);
            }

            return View();
        }
    }
}
