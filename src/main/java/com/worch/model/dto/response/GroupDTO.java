package com.worch.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Группа")
public class GroupDTO {

    @Schema(description = "Уникальный идентификатор группы", required = true)
    private UUID id;

    @Schema(description = "Название группы", required = true)
    private String name;

    @Schema(description = "Информация о владельце группы", required = true)
    private GroupOwnerDto owner;

    @Schema(description = "Дата и время создания группы", required = true)
    private OffsetDateTime createdAt;
}