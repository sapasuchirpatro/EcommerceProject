package com.ecommerce.project.service;

import com.ecommerce.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {

    AddressDTO createNewAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getUserAddresses();

    AddressDTO updateAddress(AddressDTO addressDTO, Long addressId);

    String deleteUserAddress(Long addressId);
}
