using LeaseLogic.Models;
using LeaseLogic.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
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
            property.Price = 0;

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
        public async Task<IActionResult> Edit(PropertyModel property, List<IFormFile>? imageFiles, int[]? KeepImageIndexes)
        {
            var existingProperty = await _firebaseService.GetPropertyByIdAsync(property.Id);
            if (existingProperty == null) return NotFound();

            existingProperty.Name = property.Name;
            existingProperty.Price = property.Price;
            existingProperty.Size = property.Size;
            existingProperty.Location = property.Location;
            existingProperty.Description = property.Description;
            existingProperty.Status = property.Status;

            // Keep selected images
            existingProperty.Images = KeepImageIndexes != null && KeepImageIndexes.Any()
                ? KeepImageIndexes.Select(i => existingProperty.Images[i]).ToList()
                : new List<byte[]>();

            // Append new uploaded images
            if (imageFiles != null && imageFiles.Any())
            {
                if (existingProperty.Images == null) existingProperty.Images = new List<byte[]>();

                foreach (var file in imageFiles)
                {
                    using var ms = new MemoryStream();
                    await file.CopyToAsync(ms);
                    existingProperty.Images.Add(ms.ToArray());
                }
            }

            await _firebaseService.UpdatePropertyAsync(existingProperty);

            return RedirectToAction("Index");
        }







        // ---------------- DELETE PROPERTY ----------------
        [HttpPost]
        public async Task<IActionResult> Delete(string id)
        {
            await _firebaseService.DeletePropertyAsync(id, softDelete: true);
            return RedirectToAction("Index");
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteImage(string propertyId, int imageIndex)
        {
            var property = await _firebaseService.GetPropertyByIdAsync(propertyId);
            if (property == null) return NotFound();

            if (imageIndex >= 0 && imageIndex < property.Images.Count)
            {   
                property.Images.RemoveAt(imageIndex);
                await _firebaseService.UpdatePropertyAsync(property);
            }

            return RedirectToAction("Edit", new { id = propertyId });
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

            ViewBag.TotalRent = properties.Where(p => p.Status == "Occupied").Sum(p => p.Price);

            return View();
        }

        
    }
}
