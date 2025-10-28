namespace LeaseLogic.Models
{
    public class DashboardViewModel
    {
        public double OccupancyPercentage { get; set; }
        public double RentArrears { get; set; }
        public int OpenMaintenanceRequests { get; set; }
        public int LeaseExpiringSoon { get; set; }
    }
}
