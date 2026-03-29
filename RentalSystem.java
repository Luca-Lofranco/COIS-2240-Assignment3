import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.Scanner;

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
    
    
//    loading methods
    
    
    private void loadData() {
        loadVehicles();
        loadCustomers();
        loadRentalRecords();
    }
    
//    reads vehicle file
    private void loadVehicles() {
        try (Scanner reader = new Scanner(new FileReader(VEHICLES_FILE))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] parts = line.split("\\|");
                String type   = parts[0];
                String plate  = parts[1];
                String make   = parts[2];
                String model  = parts[3];
                int    year   = Integer.parseInt(parts[4]);
                Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(parts[5]);
 
                Vehicle vehicle = null;
 
                switch (type) {
                    case "SPORTCAR" -> {
                        int     numSeats   = Integer.parseInt(parts[6]);
                        int     horsepower = Integer.parseInt(parts[7]);
                        boolean hasTurbo   = Boolean.parseBoolean(parts[8]);
                        vehicle = new SportCar(make, model, year, numSeats, horsepower, hasTurbo);
                    }
                    case "CAR" -> {
                        int numSeats = Integer.parseInt(parts[6]);
                        vehicle = new Car(make, model, year, numSeats);
                    }
                    case "MINIBUS" -> {
                        boolean isAccessible = Boolean.parseBoolean(parts[6]);
                        vehicle = new Minibus(make, model, year, isAccessible);
                    }
                    case "PICKUPTRUCK" -> {
                        double  cargoSize  = Double.parseDouble(parts[6]);
                        boolean hasTrailer = Boolean.parseBoolean(parts[7]);
                        vehicle = new PickupTruck(make, model, year, cargoSize, hasTrailer);
                    }
                }
 
                if (vehicle != null) {
                    vehicle.setLicensePlate(plate);
                    vehicle.setStatus(status);
                    vehicles.add(vehicle);
                }
            }
        } catch (IOException e) {
            System.out.println("No existing vehicles file found. Starting fresh.");
        }
    }
    
    
//    reads customer file
    private void loadCustomers() {
        try (Scanner reader = new Scanner(new FileReader(CUSTOMERS_FILE))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] parts = line.split("\\|");
                int    id   = Integer.parseInt(parts[0]);
                String name = parts[1];
                customers.add(new Customer(id, name));
            }
        } catch (IOException e) {
            System.out.println("No existing customers file found. Starting fresh.");
        }
    }
 
//    reads rental records file
    private void loadRentalRecords() {
        try (Scanner reader = new Scanner(new FileReader(RECORDS_FILE))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] parts      = line.split("\\|");
                String   recordType = parts[0];
                String   plate      = parts[1];
                int      customerId = Integer.parseInt(parts[2]);
                LocalDate date      = LocalDate.parse(parts[4]);
                double   amount     = Double.parseDouble(parts[5]);
 
                Vehicle  vehicle  = findVehicleByPlate(plate);
                Customer customer = findCustomerById(customerId);
 
                if (vehicle != null && customer != null) {
                    rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, recordType));
                } else {
                    System.out.println("Skipping record — unresolved vehicle or customer: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("No existing rental records file found. Starting fresh.");
        }
    }
    
    
    
//    saving text files
    
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
    
    public boolean addVehicle(Vehicle vehicle) {
    	if(findVehicleByPlate(vehicle.getLicensePlate()) != null) {
    		System.out.println("Error: license plate " + vehicle.getLicensePlate() + " already exists.");
    		return false;
    	}
    	return true;
    }

    public void addCustomer(Customer customer) {
        if(findCustomerById(customer.getCustomerId()) != null) {
    		System.out.println("Error: customer id " + customer.getCustomerId() + " already exists.");

        }
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