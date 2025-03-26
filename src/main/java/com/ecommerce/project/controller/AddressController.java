package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @PostMapping("addresses")
    public ResponseEntity<AddressDTO> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO savedAddressDTO = addressService.createNewAddress(addressDTO);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        List<AddressDTO> addressDTOS = addressService.getAllAddresses();
        return new ResponseEntity<>(addressDTOS, HttpStatus.OK);
    }

    @GetMapping("addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        AddressDTO addressDTO = addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses() {
        List<AddressDTO> addresses = addressService.getUserAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PutMapping("addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateUserAddress(@RequestBody AddressDTO addressDTO, @PathVariable Long addressId) {
        AddressDTO updatedAddress = addressService.updateAddress(addressDTO, addressId);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("addresses/{addressId}")
    public ResponseEntity<String> deleteUserAddress(@PathVariable Long addressId) {
        String deleteAddressResponse = addressService.deleteUserAddress(addressId);
        return new ResponseEntity<>(deleteAddressResponse, HttpStatus.OK);
    }
}
