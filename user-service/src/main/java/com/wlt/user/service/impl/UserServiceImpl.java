package com.wlt.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wlt.user.constants.MessageEnum;
import com.wlt.user.dto.UserCreatedEvent;
import com.wlt.user.dto.UserRegisterRequestDto;
import com.wlt.user.dto.UserRegisterResponseDto;
import com.wlt.user.entity.Role;
import com.wlt.user.entity.User;
import com.wlt.user.exception.CustomException;
import com.wlt.user.repository.UserRepository;
import com.wlt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.user}") // Define the exchange name in application.properties/yml
    private String userExchange;

    @Value("${rabbitmq.routing-key.user-created}") // Define the routing key in application.properties/yml
    private String userCreatedRoutingKey;

    @Override
    @SneakyThrows
    public UserRegisterResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {
        Optional<User> user = userRepository.findByEmail(userRegisterRequestDto.getEmail());
        if (user.isPresent()) {
            throw new CustomException(MessageEnum.EMAIL_ALREADY_TAKEN);
        }

        User userEntity = new User();
        userEntity.setEmail(userRegisterRequestDto.getEmail());
        userEntity.setPasswordHash(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        userEntity.setEnabled(true);
        userEntity.setRoles(Set.of(Role.USER));
        User userCreated = userRepository.save(userEntity);
        UserRegisterResponseDto response = new UserRegisterResponseDto();
        response.setUsername(userRegisterRequestDto.getEmail());

        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setUserId(userCreated.getId());

//        String jsonString = objectMapper.writeValueAsString(userCreatedEvent);
//        rabbitTemplate.convertAndSend(userExchange, userCreatedRoutingKey, userCreatedEvent);

        return response;
    }
}
