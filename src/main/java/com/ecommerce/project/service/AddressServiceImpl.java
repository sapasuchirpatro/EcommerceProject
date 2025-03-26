package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public AddressDTO createNewAddress(AddressDTO addressDTO) {
        User currentUserDetails = authUtil.loggedInUser();

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(currentUserDetails);
        Address savedAddress = addressRepository.save(address);

        addressDTO = modelMapper.map(savedAddress, AddressDTO.class);
        return addressDTO;
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        User currentUserDetails = authUtil.loggedInUser();
        List<Address> addresses = addressRepository.findAll();
//        List<AddressDTO> addressDTOS = addresses.stream()
//                .map(address -> {
//                    AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
//                    return addressDTO;
//                })
//                .toList();

        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
        return addressDTOS;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
        return addressDTO;
    }

    @Override
    public List<AddressDTO> getUserAddresses() {
        User user = authUtil.loggedInUser();
        List<Address> addresses = user.getAddresses();

        List<AddressDTO> addressDTOS =  addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address addressToUpdate = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

//        addressToUpdate.setBuildingName(addressDTO.getBuildingName());
//        addressToUpdate.setStreet(addressDTO.getStreet());
//        addressToUpdate.setCity(addressDTO.getCity());
//        addressToUpdate.setState(addressDTO.getState());
//        addressToUpdate.setCountry(addressDTO.getCountry());
//        addressToUpdate.setPinCode(addressDTO.getPinCode());

        modelMapper.map(addressDTO, addressToUpdate);

        addressToUpdate.setAddressId(addressId);
        Address updatedAddress = addressRepository.save(addressToUpdate);

        // The below logic is required to update the address in addresses list in user entity in the memory
        User user = addressToUpdate.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        AddressDTO updatedAddressDTO = modelMapper.map(updatedAddress, AddressDTO.class);
        return updatedAddressDTO;
    }

    @Override
    public String deleteUserAddress(Long addressId) {
        Address addressToDelete = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        // The below logic is required to delete the address in addresses list in user entity in the memory
        User user = addressToDelete.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.deleteById(addressId);
        return "Address with id: " + addressId + " has been deleted";
    }


}
