package com.worch.service;

import com.worch.exceptions.ChannelNotFoundException;
import com.worch.model.dto.request.CreateChannelRequest;
import com.worch.model.dto.request.UpdateChannelRequest;
import com.worch.model.entity.Channel;
import com.worch.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final PasswordEncoder passwordEncoder;
    private final ChannelRepository channelRepository;

    public Channel createChannel(CreateChannelRequest request) {
        Channel channel = Channel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(request.getOwner())
                .isPrivate(request.getIsPrivate())
                .ageRestricted(request.getAgeRestricted())
                .build();

        if (request.getIsPrivate()) {
            channel.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return channelRepository.save(channel);
    }

    public Channel findChannel(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException("No such channel with id: " + id));
    }

    public Channel findChannelWithMembers(UUID id) {
        return channelRepository.findWithMembersById(id)
                .orElseThrow(() -> new ChannelNotFoundException("No such channel with id: " + id));
    }

    public void deleteChannel(UUID id) {
        channelRepository.deleteById(id);
    }

    public Channel updateChannel(UUID id, UpdateChannelRequest request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException("No such channel with id: " + id));

        channel.setName(request.getName());
        channel.setDescription(request.getDescription());
        channel.setIsPrivate(request.getIsPrivate());
        channel.setAgeRestricted(request.getAgeRestricted());

        if (request.getIsPrivate() && request.getPassword() != null) {
            channel.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return channelRepository.save(channel);
    }

    public Channel getReferenceById(UUID id) {
        return channelRepository.getReferenceById(id);
    }
}