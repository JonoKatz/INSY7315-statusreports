using Microsoft.AspNetCore.Mvc;

namespace LeaseLogic.Controllers
{
    public class MaintenanceController : BaseController
    {
        public IActionResult List()
        {
            return View();
        }

        public IActionResult Details(string id)
        {
            return View();
        }
    }
}
