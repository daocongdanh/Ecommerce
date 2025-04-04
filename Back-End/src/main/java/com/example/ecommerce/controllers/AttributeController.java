package com.example.ecommerce.controllers;

import com.example.ecommerce.dtos.AttributeDTO;
import com.example.ecommerce.models.Attribute;
import com.example.ecommerce.responses.ResponseSuccess;
import com.example.ecommerce.services.attribute.AttributeService;
import com.example.ecommerce.utils.Translator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/attributes")
@RequiredArgsConstructor
public class AttributeController {
    private final AttributeService attributeService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> createAttribute(
            @Valid @RequestBody AttributeDTO attributeDTO){
        Attribute attribute = attributeService.createAttribute(attributeDTO);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message(Translator.toLocale("attribute.create.success"))
                .status(HttpStatus.CREATED.value())
                .data(attribute)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseSuccess> getAttributeById(@PathVariable("id") long id){
        Attribute attribute = attributeService.getAttributeById(id);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message(Translator.toLocale("attribute.get_by_id.success"))
                .status(HttpStatus.OK.value())
                .data(attribute)
                .build());
    }

    @GetMapping("")
    public ResponseEntity<ResponseSuccess> getAllAttributes(
            @RequestParam(value = "category", required = false) String cname,
            @RequestParam(value = "brand", required = false) String bname){
        List<?> attributes = attributeService.getAllAttributes(cname, bname);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message(Translator.toLocale("attribute.get_all.success"))
                .status(HttpStatus.OK.value())
                .data(attributes)
                .build());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> updateAttribute(@PathVariable long id,
                                                       @Valid @RequestBody AttributeDTO attributeDTO){
        Attribute attribute = attributeService.updateAttribute(id, attributeDTO);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message(Translator.toLocale("attribute.update.success"))
                .status(HttpStatus.OK.value())
                .data(attribute)
                .build());
    }
}
