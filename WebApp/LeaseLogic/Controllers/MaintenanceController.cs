using LeaseLogic.Models;
using LeaseLogic.Services;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using System.Linq;

namespace LeaseLogic.Controllers
{
    public class MaintenanceController : Controller
    {
        private readonly FirebaseService _firebaseService = new FirebaseService();

        // View-only list of maintenance requests
        public async Task<IActionResult> List(string? tenantId, string? status)
        {
            bool? onlyCompleted = null;

            if (!string.IsNullOrEmpty(status))
            {
                if (status == "Completed") onlyCompleted = true;
                else if (status == "Active") onlyCompleted = false; // Pending + In Progress
            }

            var requests = await _firebaseService.GetMaintenanceRequestsAsync(tenantId, onlyCompleted);

            if (!string.IsNullOrEmpty(status) && status != "Completed" && status != "Active")
            {
                // Filter by specific status if given: Pending / In Progress
                requests = requests.Where(r => r.Status == status).ToList();
            }

            return View(requests.OrderByDescending(r => r.Timestamp).ToList());
        }
    }
}
