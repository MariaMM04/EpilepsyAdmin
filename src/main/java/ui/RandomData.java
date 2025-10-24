package ui;

import org.example.entities_medicaldb.*;
import java.time.LocalDate;
import java.util.Random;


public class RandomData {

    public enum SymptomType {
        Muscle_Stiffness,
        Trouble_Breathing,
        Sudden_Unconsciousness,
        Auditory_Hallucinations,
        Anxiety,
        Deja_vu,
        Short_term_confusion,
        Dizziness,
        Loss_off_balance,
        Fear,
        Nausea,
        Muscle_spasms,
        Fatigue,
        None
    }

    public static Patient generateRandomPatient() {
        String[] POSSIBLE_NAMES = {"Alice", "Bob", "Charlie", "David", "Eve"};
        String[] POSSIBLE_SURNAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] POSSIBLE_SEXES = {"Male", "Female"};
        String EMAIL_DOMAIN = "@example.com";  // Fixed domain for email generation
        Random random = new Random();
        // Generate random values
        String name = POSSIBLE_NAMES[random.nextInt(POSSIBLE_NAMES.length)];
        String surname = POSSIBLE_SURNAMES[random.nextInt(POSSIBLE_SURNAMES.length)];
        String email = name.toLowerCase() + "." + surname.toLowerCase() + EMAIL_DOMAIN;
        Integer phone = 600000000 + random.nextInt(100000000);  // Random 10-digit number (1,000,000,000 to 1,999,999,999)
        String sex = POSSIBLE_SEXES[random.nextInt(POSSIBLE_SEXES.length)];
        // Create and populate the Patient object
        Patient patient = new Patient();
        patient.setName(name);
        patient.setSurname(surname);
        patient.setEmail(email);
        patient.setContact(phone.toString());  // Assuming setPhone accepts an Integer
        patient.setGender(sex);
        patient.setDateOfBirth(generateRandomLocalDate(new Random()));
        return patient;
    }

    public static Doctor generateRandomDoctor() {
        String[] POSSIBLE_NAMES = {"Alice", "Bob", "Charlie", "David", "Eve"};
        String[] POSSIBLE_SURNAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] POSSIBLE_ADDRESSES = {
                "123 Main St, Anytown",
                "456 Oak Ave, Othercity",
                "789 Pine Rd, Sometown",
                "101 Elm Blvd, Bigtown",
                "202 Maple Ln, Smallville"
        };
        String EMAIL_DOMAIN = "@example.com";  // Fixed domain for email generation

        Random random = new Random();
        // Generate random values
        String name = POSSIBLE_NAMES[random.nextInt(POSSIBLE_NAMES.length)];
        String surname = POSSIBLE_SURNAMES[random.nextInt(POSSIBLE_SURNAMES.length)];
        String email = name.toLowerCase() + "." + surname.toLowerCase() + EMAIL_DOMAIN;
        Integer phone = 900000000 + random.nextInt(100000000);  // Random 10-digit number (1,000,000,000 to 1,999,999,999)
        String address = POSSIBLE_ADDRESSES[random.nextInt(POSSIBLE_ADDRESSES.length)];
        // Create and populate the Doctor object
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSurname(surname);
        doctor.setEmail(email);
        doctor.setContact(phone.toString());  // Assuming setPhone accepts an Integer
        doctor.setDepartment(address);
        doctor.setSpeciality("Neurology");
        return doctor;
    }


    // Helper method to generate a random date in "YYYY-MM-DD" format
    private static String generateRandomDate(Random random) {
        //int year = 2000 + random.nextInt(24);  // 2000 to 2023
        int year = 2025;
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 to avoid invalid dates
        return String.format("%02d/%02d/%04d", day, month, year);
    }
    private static LocalDate generateRandomLocalDate(Random random) {
        //int year = 2000 + random.nextInt(24);  // 2000 to 2023
        int year = 2025;
        int month = 1 + random.nextInt(12);    // 1 to 12
        int day = 1 + random.nextInt(28);      // 1 to 28 to avoid invalid dates
        return LocalDate.of(year, month, day);
    }

    public static void main(String[] args) {
        Patient randomPatient = RandomData.generateRandomPatient();
        Doctor randomDoctor = RandomData.generateRandomDoctor();
        System.out.println(randomPatient);
        System.out.println(randomDoctor);// Prints the generated Patient object

        /*ArrayList<Report> randomReports = generateRandomReports();
        for (Report report : randomReports) {
            System.out.println(report);  // Prints each report using its toString method
        }*/
    }
}
