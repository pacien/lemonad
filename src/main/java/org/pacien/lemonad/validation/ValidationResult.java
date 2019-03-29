/*
 * lemonad - Some functional sweetness for Java
 * Copyright (C) 2019  Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.lemonad.validation;

import org.pacien.lemonad.attempt.Attempt;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.NonNull;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.pacien.lemonad.attempt.Attempt.failure;
import static org.pacien.lemonad.attempt.Attempt.success;

/**
 * Wraps the result of the validation of a subject.
 *
 * @param <S> the subject type,
 * @param <E> the error type.
 * @author pacien
 */
public interface ValidationResult<S, E> {
  /**
   * @return whether no error have been reported during the validation.
   */
  boolean isValid();

  /**
   * @return whether some error have been reported during the validation.
   */
  boolean isInvalid();

  /**
   * @return the subject of the validation.
   */
  S getSubject();

  /**
   * @return the potentially empty list of reported validation errors.
   */
  List<E> getErrors();

  /**
   * @param consumer a subject consumer called if the validation is successful.
   * @return the current object.
   */
  default ValidationResult<S, E> ifValid(@NonNull Consumer<? super S> consumer) {
    if (isValid()) consumer.accept(getSubject());
    return this;
  }

  /**
   * @param consumer the consumer called with the validation subject and reported errors if the validation is failed.
   * @return the current object.
   */
  default ValidationResult<S, E> ifInvalid(@NonNull BiConsumer<? super S, ? super List<? super E>> consumer) {
    if (!isValid()) consumer.accept(getSubject(), getErrors());
    return this;
  }

  /**
   * @return an {@link Attempt} with a state corresponding to the one of the validation.
   */
  default Attempt<S, List<E>> toAttempt() {
    return isValid() ? success(getSubject()) : failure(getErrors());
  }

  /**
   * @param mapper a function transforming a {@link ValidationResult}.
   * @return the transformed {@link ValidationResult}.
   */
  default <SS, EE> ValidationResult<SS, EE> flatMap(@NonNull Function<? super ValidationResult<? super S, ? super E>, ? extends ValidationResult<? extends SS, ? extends EE>> mapper) {
    //noinspection unchecked
    return (ValidationResult<SS, EE>) mapper.apply(this);
  }

  /**
   * @param subject           an overriding subject.
   * @param validationResults a {@link Stream} of {@link ValidationResult}s to merge.
   * @return the merged {@link ValidationResult} containing all errors from the supplied ones.
   */
  static <S, E> ValidationResult<S, E> merge(S subject, @NonNull Stream<? extends ValidationResult<?, ? extends E>> validationResults) {
    return new ValidationResultContainer<>(
      subject,
      validationResults.flatMap(res -> res.getErrors().stream()).collect(toUnmodifiableList()));
  }

  /**
   * @param subject the suject of the validation.
   * @return a successful {@link ValidationResult}.
   */
  static <S, E> ValidationResult<S, E> valid(S subject) {
    return new ValidationResultContainer<>(subject, List.of());
  }

  /**
   * @param subject the suject of the validation.
   * @param error   a validation error.
   * @param errors  additional validation errors.
   * @return a failed {@link ValidationResult} for the supplied subject.
   */
  @SafeVarargs static <S, E> ValidationResult<S, E> invalid(S subject, E error, E... errors) {
    return new ValidationResultContainer<>(
      subject,
      Stream.concat(Stream.of(error), Arrays.stream(errors)).map(Objects::requireNonNull).collect(toUnmodifiableList()));
  }
}
