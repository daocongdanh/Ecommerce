package com.example.ecommerce.services.address;

import com.example.ecommerce.dtos.AddressDTO;
import com.example.ecommerce.dtos.AddressUpdateDTO;
import com.example.ecommerce.exceptions.ResourceNotFoundException;
import com.example.ecommerce.models.Address;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.AddressRepository;
import com.example.ecommerce.responses.AddressResponse;
import com.example.ecommerce.services.user.UserService;
import com.example.ecommerce.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final AuthUtil authUtil;
    @Override
    @Transactional
    public AddressResponse createAddress(AddressDTO addressDTO) {
        User user = userService.findById(addressDTO.getUserId());
        authUtil.checkAuth(user.getId());
        Address address = Address.builder()
                .name(addressDTO.getName())
                .detail(addressDTO.getDetail())
                .code(addressDTO.getCode())
                .isDefault(addressDTO.isDefault())
                .user(user)
                .build();
        if(addressDTO.isDefault()){
            List<Address> addresses = addressRepository.findAllByUser(user);
            for(Address ad : addresses){
                ad.setDefault(false);
                addressRepository.save(ad);
            }
        }
        addressRepository.save(address);
        return AddressResponse.fromAddress(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(long id, AddressUpdateDTO addressUpdateDTO) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id = " + id));
        User user = address.getUser();
        authUtil.checkAuth(user.getId());
        address.setName(addressUpdateDTO.getName());
        address.setDetail(addressUpdateDTO.getDetail());
        address.setCode(addressUpdateDTO.getCode());
        address.setDefault(addressUpdateDTO.isDefault());
        if(addressUpdateDTO.isDefault()){
            List<Address> addresses = addressRepository.findAllByUser(user);
            for(Address ad : addresses){
                if(!Objects.equals(ad.getId(), address.getId())){
                    ad.setDefault(false);
                    addressRepository.save(ad);
                }
            }
        }
        addressRepository.save(address);
        return AddressResponse.fromAddress(address);
    }

    @Override
    public List<AddressResponse> getAllAddressesByUser(long userId) {
        User user = userService.findById(userId);
        return addressRepository.findAllByUser(user)
                .stream()
                .map(AddressResponse::fromAddress)
                .toList();
    }

    @Override
    public AddressResponse getAddressById(long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id = " + id));
        authUtil.checkAuth(address.getUser().getId());
        return AddressResponse.fromAddress(address);
    }

    @Override
    @Transactional
    public void deleteAddress(long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id = " + id));
        authUtil.checkAuth(address.getUser().getId());
        addressRepository.delete(address);
    }
}
