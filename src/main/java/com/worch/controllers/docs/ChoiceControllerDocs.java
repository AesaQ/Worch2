package com.worch.controllers.docs;

import com.worch.model.dto.request.VoteRequest;
import com.worch.model.dto.response.ChoiceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChoiceControllerDocs {
    @Operation(
            summary = "Get choices",
            description = "Returns a list of choices. Optionally filtered by creatorId",
            parameters = {
                    @Parameter(
                            name = "creatorId",
                            in = ParameterIn.QUERY,
                            description = "Filter choices by creator ID",
                            required = false,
                            schema = @Schema(type = "string", format = "uuid")
                    )
            }
    )
    ResponseEntity<List<ChoiceResponseDto>> getChoices(@RequestParam(required = false) Optional<UUID> creatorId);

    @Operation(
            summary = "Vote for choice option",
            parameters = {
                    @Parameter(
                            name = "Idempotency-Key",
                            in = ParameterIn.HEADER,
                            description = "Unique key to make request idempotent",
                            required = false,
                            schema = @Schema(type = "string")
                    )
            }
    )
    ResponseEntity<String> vote(@RequestBody VoteRequest voteRequest,
                                @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey);
}
