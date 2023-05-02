package com.carBooking.Booking;

import com.carBooking.Car.Car;
import com.carBooking.Car.CarService;
import com.carBooking.User.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CarBookingService {
    private final CarBookingDAO carBookingDAO;
    private final CarService carService;

    public CarBookingService(CarBookingDAO carBookingDAO, CarService carService) {
        this.carBookingDAO = carBookingDAO;
        this.carService = carService;
    }

    public UUID bookCar(User user, String regNumber) {
        List<Car> availableCars = getAvailableCars();

        if (availableCars.isEmpty()) {
            throw new IllegalStateException("No car available for renting");
        }

        for (Car availableCar : availableCars) {
            // making sure the car that the user wants is still available
            if (availableCar.getRegNumber().equals(regNumber)) {
                Car car = carService.getCar(regNumber);
                UUID bookingId = UUID.randomUUID();

                carBookingDAO.book(new CarBooking(bookingId, user, car, LocalDateTime.now()));

                return bookingId;
            }
        }
        throw new IllegalStateException("Already booked. car with regNumber " + regNumber);
    }

    public List<Car> getUserBookedCars(UUID userId) {
        List<CarBooking> carBookings = carBookingDAO.getCarBookings();
        List<Car> userCars = new ArrayList<>();

        for (CarBooking carBooking : carBookings) {
            if (carBooking != null && carBooking.getUser().getId().equals(userId)) {
                userCars.add(carBooking.getCar());
            }
        }
        return userCars;
    }

    public List<Car> getAvailableCars() {
        return getCars(carService.getAllCars());
    }

    public List<Car> getAvailableElectricCars() {
        return getCars(carService.getAllElectricCars());
    }

    private List<Car> getCars(List<Car> cars) {
        // no cars in the system yet
        if (cars.isEmpty()) {
            return Collections.emptyList();
        }

        List<CarBooking> carBookings = carBookingDAO.getCarBookings();

        // no bookings yet therefore all cars all available
        if (carBookings.isEmpty()) {
            return cars;
        }

        List<Car> availableCars = new ArrayList<>();

        // populate available cars
        for (Car car : cars) {
            // check if car is part of any booking
            // if not, then add it to available cars
            boolean booked = false;
            for (CarBooking carBooking : carBookings) {
                if (carBooking == null || !carBooking.getCar().equals(car)) {
                    continue;
                }
                booked = true;
            }

            if (!booked) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }

    public List<CarBooking> getBookings() {
        return carBookingDAO.getCarBookings();
    }
}