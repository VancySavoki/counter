package org.trivia.counter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Result<T> {
    String status;
    String msg;
    String error;
    T data;
}