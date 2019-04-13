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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.NonNull;

import static java.util.function.Function.identity;
import static org.pacien.lemonad.attempt.Attempt.failure;
import static org.pacien.lemonad.attempt.Attempt.success;

/**
 * Wraps the result of the validation of a subject.
 *
 * @param <S> the subject type,
 * @param <E> the error type.
 * @author pacien
 */
public interface Validation<S, E> {
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
  default Validation<S, E> ifValid(@NonNull Consumer<? super S> consumer) {
    if (isValid()) consumer.accept(getSubject());
    return this;
  }

  /**
   * @param consumer the consumer called with the validation subject and reported errors if the validation has failed.
   * @return the current object.
   */
  default Validation<S, E> ifInvalid(@NonNull BiConsumer<? super S, ? super List<? super E>> consumer) {
    if (isInvalid()) consumer.accept(getSubject(), getErrors());
    return this;
  }

  /**
   * @param predicate the validation predicate testing the validity of a subject.
   * @param error     the error to return if the subject does not pass the test.
   * @return an updated {@link Validation}.
   */
  default Validation<S, E> validate(@NonNull Predicate<? super S> predicate, @NonNull E error) {
    return validate(identity(), predicate, error);
  }

  /**
   * @param mapper    the field getter mapping the validation subject.
   * @param predicate the validation predicate testing the validity of a subject.
   * @param error     the error to return if the subject does not pass the test.
   * @return an updated {@link Validation}.
   */
  default <F> Validation<S, E> validate(
    @NonNull Function<? super S, ? extends F> mapper,
    @NonNull Predicate<? super F> predicate,
    E error
  ) {
    return validate(mapper, field -> predicate.test(field) ? List.of() : List.of(error));
  }

  /**
   * @param validator the validating function to use, returning a potentially empty list of errors.
   * @return an updated {@link Validation}.
   */
  default Validation<S, E> validate(@NonNull Function<? super S, ? extends List<? extends E>> validator) {
    var errors = validator.apply(getSubject());
    return errors.isEmpty() ? this : merge(errors);
  }

  /**
   * @param mapper    the field getter mapping the validation subject.
   * @param validator the validating function to use, returning a potentially empty list of errors.
   * @return an updated {@link Validation}.
   */
  default <F> Validation<S, E> validate(
    @NonNull Function<? super S, ? extends F> mapper,
    @NonNull Function<? super F, ? extends List<? extends E>> validator
  ) {
    return validate(validator.compose(mapper));
  }

  /**
   * @param validator a subject validating function returning a {@link Validation}.
   * @return an updated {@link Validation}.
   */
  default Validation<S, E> merge(@NonNull Function<? super S, ? extends Validation<?, ? extends E>> validator) {
    return merge(validator.apply(getSubject()));
  }

  /**
   * @param mapper    the field getter mapping the validation subject.
   * @param validator a subject validating function returning a {@link Validation}.
   * @return an updated {@link Validation}.
   */
  default <F> Validation<S, E> merge(
    @NonNull Function<? super S, ? extends F> mapper,
    @NonNull Function<? super F, ? extends Validation<?, ? extends E>> validator
  ) {
    return merge(validator.compose(mapper));
  }

  /**
   * @param validation another validation to merge into the current one.
   * @return an updated {@link Validation}.
   */
  @SuppressWarnings("unchecked")
  default Validation<S, E> merge(@NonNull Validation<?, ? extends E> validation) {
    if (validation.isValid()) return this;
    if (this.isValid()) return Validation.of(this.getSubject(), (List<E>) validation.getErrors());
    return merge(validation.getErrors());
  }

  /**
   * @param errors a potentially empty list of additional errors to take into account.
   * @return an updated {@link Validation}.
   */
  default Validation<S, E> merge(@NonNull Collection<? extends E> errors) {
    var combinedErrors = new ArrayList<E>(getErrors().size() + errors.size());
    combinedErrors.addAll(getErrors());
    combinedErrors.addAll(errors);
    return new ValidationContainer<>(getSubject(), combinedErrors);
  }

  /**
   * @param mapper a function transforming a {@link Validation}.
   * @return the transformed {@link Validation}.
   */
  default <SS, EE> Validation<SS, EE> flatMap(
    @NonNull Function<? super Validation<? super S, ? super E>, ? extends Validation<? extends SS, ? extends EE>> mapper
  ) {
    //noinspection unchecked
    return (Validation<SS, EE>) mapper.apply(this);
  }

  /**
   * @return an {@link Attempt} with a state corresponding to the one of the validation.
   */
  default Attempt<S, List<E>> toAttempt() {
    return isValid() ? success(getSubject()) : failure(getErrors());
  }

  /**
   * @param subject the subject of the validation.
   * @param errors  some optional validation errors.
   * @return a {@link Validation}.
   */
  @SafeVarargs static <S, E> Validation<S, E> of(S subject, E... errors) {
    return Validation.of(subject, List.of(errors));
  }

  /**
   * @param subject the subject of the validation.
   * @param errors  some optional validation errors.
   * @return a {@link Validation}.
   */
  static <S, E> Validation<S, E> of(S subject, @NonNull List<E> errors) {
    return new ValidationContainer<>(subject, errors);
  }
}
