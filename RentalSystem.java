import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;


public class RentalSystem {
	private static RentalSystem instance;
	
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();
    
//    txt files
    private static final String VEHICLES_FILE  = "vehicles.txt";
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String RECORDS_FILE   = "rental_records.txt";
    
    private RentalSystem() {}
    
    public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }
    
//    saves vehicle to txt file  
    private void saveVehicle(Vehicle vehicle) {
        try (FileWriter writer = new FileWriter(VEHICLES_FILE, true)) {
            writer.write(buildVehicleLine(vehicle) + "\n");
        } catch (IOException e) {
            System.out.println("Error saving vehicle to the file: " + e.getMessage());
        }
    }
    
//    Overwrites the Vehicles txt file
    private void overwriteAllVehicles() {
        try (FileWriter writer = new FileWriter(VEHICLES_FILE, false)) {
            for (Vehicle v : vehicles) {
                writer.write(buildVehicleLine(v) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error updating vehicles to the file: " + e.getMessage());
        }
    }
    
    
    private String buildVehicleLine(Vehicle vehicle) {
        String line = "";
 
        // SportCar must be checked before Car because SportCar extends Car
        if (vehicle instanceof SportCar sc) {
            line = "SPORTCAR|" + sc.getLicensePlate() + "|" + sc.getMake() + "|"
                 + sc.getModel() + "|" + sc.getYear() + "|" + sc.getStatus() + "|"
                 + sc.getNumSeats() + "|" + sc.getHorsepower() + "|" + sc.hasTurbo();
            
        } else if (vehicle instanceof Car c) {
            line = "CAR|" + c.getLicensePlate() + "|" + c.getMake() + "|"
                 + c.getModel() + "|" + c.getYear() + "|" + c.getStatus() + "|"
                 + c.getNumSeats();
            
        } else if (vehicle instanceof Minibus mb) {
            line = "MINIBUS|" + mb.getLicensePlate() + "|" + mb.getMake() + "|"
            + mb.getModel() + "|" + mb.getYear() + "|" + mb.getStatus() + "|" + mb.isAccessible();
            
        } else if (vehicle instanceof PickupTruck pt) {
            line = "PICKUPTRUCK|" + pt.getLicensePlate() + "|" + pt.getMake() + "|"
            + pt.getModel() + "|" + pt.getYear() + "|" + pt.getStatus() + "|"
            + pt.getCargoSize() + "|" + pt.hasTrailer();
        } else {
            line = "VEHICLE|" + vehicle.getLicensePlate() + "|" + vehicle.getMake() + "|"
            + vehicle.getModel() + "|" + vehicle.getYear() + "|" + vehicle.getStatus();
        }
 
        return line;
    }
    
    private void saveCustomer(Customer customer) {
        try (FileWriter writer = new FileWriter(CUSTOMERS_FILE, true)) {
            writer.write(customer.getCustomerId() + "|" + customer.getCustomerName() + "\n");
        } catch (IOException e) {
            System.out.println("Error saving customer to the file: " + e.getMessage());
        }
    }
    
    private void saveRecord(RentalRecord record) {
        try (FileWriter writer = new FileWriter(RECORDS_FILE, true)) {
            String line = record.getRecordType() + "|"
                        + record.getVehicle().getLicensePlate() + "|"
                        + record.getCustomer().getCustomerId() + "|"
                        + record.getCustomer().getCustomerName() + "|"
                        + record.getRecordDate() + "|"
                        + record.getTotalAmount();
            writer.write(line + "\n");
        } catch (IOException e) {
            System.out.println("Error saving rental record to file: " + e.getMessage());
        }
    }
    
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Available) {
            vehicle.setStatus(Vehicle.VehicleStatus.Rented);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, "RENT"));
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Rented) {
            vehicle.setStatus(Vehicle.VehicleStatus.Available);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, extraFees, "RETURN"));
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    public void displayVehicles(Vehicle.VehicleStatus status) {
        // Display appropriate title based on status
        if (status == null) {
            System.out.println("\n=== All Vehicles ===");
        } else {
            System.out.println("\n=== " + status + " Vehicles ===");
        }
        
        // Header with proper column widths
        System.out.printf("|%-16s | %-12s | %-12s | %-12s | %-6s | %-18s |%n", 
            " Type", "Plate", "Make", "Model", "Year", "Status");
        System.out.println("|--------------------------------------------------------------------------------------------|");
    	  
        boolean found = false;
        for (Vehicle vehicle : vehicles) {
            if (status == null || vehicle.getStatus() == status) {
                found = true;
                String vehicleType;
                if (vehicle instanceof Car) {
                    vehicleType = "Car";
                } else if (vehicle instanceof Minibus) {
                    vehicleType = "Minibus";
                } else if (vehicle instanceof PickupTruck) {
                    vehicleType = "Pickup Truck";
                } else {
                    vehicleType = "Unknown";
                }
                System.out.printf("| %-15s | %-12s | %-12s | %-12s | %-6d | %-18s |%n", 
                    vehicleType, vehicle.getLicensePlate(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus().toString());
            }
        }
        if (!found) {
            if (status == null) {
                System.out.println("  No Vehicles found.");
            } else {
                System.out.println("  No vehicles with Status: " + status);
            }
        }
        System.out.println();
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        if (rentalHistory.getRentalHistory().isEmpty()) {
            System.out.println("  No rental history found.");
        } else {
            // Header with proper column widths
            System.out.printf("|%-10s | %-12s | %-20s | %-12s | %-12s |%n", 
                " Type", "Plate", "Customer", "Date", "Amount");
            System.out.println("|-------------------------------------------------------------------------------|");
            
            for (RentalRecord record : rentalHistory.getRentalHistory()) {                
                System.out.printf("| %-9s | %-12s | %-20s | %-12s | $%-11.2f |%n", 
                    record.getRecordType(), 
                    record.getVehicle().getLicensePlate(),
                    record.getCustomer().getCustomerName(),
                    record.getRecordDate().toString(),
                    record.getTotalAmount()
                );
            }
            System.out.println();
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }
}