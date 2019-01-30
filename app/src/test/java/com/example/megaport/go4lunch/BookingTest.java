package com.example.megaport.go4lunch;

import com.example.megaport.go4lunch.main.Models.Booking;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BookingTest {

    private Booking booking;

    @Before
    public void setUp() throws Exception {
        booking = new Booking("24/09/2018","1234", "5678","Test_Name");
    }

    @Test
    public void getBookingInfo(){
        assertEquals("24/09/2018", booking.getBookingDate());
        assertEquals("1234", booking.getUserId());
        assertEquals("5678", booking.getRestaurantId());
        assertEquals("Test_Name", booking.getRestaurantName());
    }

    @Test
    public void setBookingInfo() {
        booking.setBookingDate("25/10/2019");
        booking.setUserId("1111");
        booking.setRestaurantId("9999");
        booking.setRestaurantName("RestaurantName");

        assertEquals("25/10/2019", booking.getBookingDate());
        assertEquals("1111", booking.getUserId());
        assertEquals("9999", booking.getRestaurantId());
        assertEquals("RestaurantName", booking.getRestaurantName());
    }
}