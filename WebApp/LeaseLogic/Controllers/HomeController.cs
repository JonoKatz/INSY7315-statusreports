using System.Diagnostics;
using LeaseLogic.Models;
using Microsoft.AspNetCore.Mvc;

namespace LeaseLogic.Controllers
{
    public class HomeController : BaseController
    {
        public IActionResult Index()
        {
            return View();
        }
    }
}
