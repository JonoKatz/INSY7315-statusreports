using LeaseLogic.Models;
using LeaseLogic.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace LeaseLogic.Controllers
{
    public class PropertyController : Controller
    {
        private readonly FirebaseService _firebaseService = new FirebaseService();

        // ---------------- PROPERTY LIST ----------------
        public async Task<IActionResult> Index()
        {
            // Only fetch properties that are not soft-deleted
            var properties = await _firebaseService.GetPropertiesAsync(includeDeleted: false);
            return View(properties);
        }

        // ---------------- ADD PROPERTY ----------------
        [HttpGet]
        public IActionResult Add()
        {
            return View(new PropertyModel());
        }

        [HttpPost]
        public async Task<IActionResult> Add(PropertyModel property, List<IFormFile>? imageFiles)
        {
            if (!ModelState.IsValid || string.IsNullOrWhiteSpace(property.Description))
            {
                ViewBag.Error = "Description is required";
                return View(property);
            }

            property.Id = Guid.NewGuid().ToString();
            property.IsRented = false; // default
            property.Price = 0; // rent only when occupied

            var success = await _firebaseService.AddPropertyAsync(property, imageFiles);
            if (!success)
            {
                ViewBag.Error = "Failed to save property";
                return View(property);
            }

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
        public async Task<IActionResult> Edit(PropertyModel property, List<IFormFile>? imageFiles)
        {
            if (!ModelState.IsValid || string.IsNullOrWhiteSpace(property.Description))
            {
                ViewBag.Error = "Description is required";
                return View(property);
            }

            var success = await _firebaseService.UpdatePropertyAsync(property);
            if (!success)
            {
                ViewBag.Error = "Failed to update property";
                return View(property);
            }

            // Add new images if any
            if (imageFiles != null && imageFiles.Any())
            {
                await _firebaseService.AddPropertyAsync(property, imageFiles);
            }

            return RedirectToAction("Index");
        }

        // ---------------- DELETE PROPERTY ----------------
        [HttpPost]
        public async Task<IActionResult> Delete(string id)
        {
            await _firebaseService.DeletePropertyAsync(id, softDelete: true);
            return RedirectToAction("Index");
        }

        // ---------------- VIEW PROPERTY ----------------
        [HttpGet]
        public async Task<IActionResult> View(string id)
        {
            var property = await _firebaseService.GetPropertyByIdAsync(id);
            if (property == null) return NotFound();
            return View(property);
        }

        // ---------------- DASHBOARD ----------------
        public async Task<IActionResult> Dashboard()
        {
            var properties = await _firebaseService.GetPropertiesAsync(includeDeleted: false);

            if (!properties.Any())
            {
                ViewBag.TotalRent = 0;
            }
            else
            {
                // Only sum rent for rented properties
                ViewBag.TotalRent = properties.Where(p => p.IsRented).Sum(p => p.Price);
            }

            return View();
        }
    }
}
