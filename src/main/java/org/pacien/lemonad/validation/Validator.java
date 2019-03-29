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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.NonNull;
import lombok.val;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.pacien.lemonad.validation.Validation.invalid;
import static org.pacien.lemonad.validation.Validation.valid;

/**
 * A function which applies validation rules on a subject and reports possible errors.
 *
 * @param <S> the subject type
 * @param <E> the error type
 * @author pacien
 */
@FunctionalInterface public interface Validator<S, E> {
  /**
   * @param subject the subject to validate, which can potentially be null.
   * @return the non-null result of the validation of the supplied subject.
   */
  Validation<S, E> validate(S subject);

  /**
   * @param predicate     the validation predicate testing the validity of a subject.
   * @param negativeError an error to return if the subject does not pass the test.
   * @return a {@link Validator} based on the supplied predicate and error.
   */
  static <S, E> Validator<S, E> ensuringPredicate(@NonNull Predicate<? super S> predicate, @NonNull E negativeError) {
    return subject -> predicate.test(subject) ? valid(subject) : invalid(subject, negativeError);
  }

  /**
   * @param validators the {@link Validator}s to combine, to be evaluated in order of listing.
   * @return a {@link Validator} based on the supplied ones.
   */
  @SafeVarargs static <S, E> Validator<S, E> validatingAll(@NonNull Validator<? super S, ? extends E>... validators) {
    val validatorList = Arrays.stream(validators).map(Objects::requireNonNull).collect(toUnmodifiableList());
    return subject -> new ValidationContainer<>(
      subject,
      validatorList.stream()
                   .flatMap(validator -> validator.validate(subject).getErrors().stream())
                   .collect(toUnmodifiableList()));
  }

  /**
   * @param getter    the field getter mapping the validation subject.
   * @param validator the {@link Validator} validating the field.
   * @return a {@link Validator} validating the parent object.
   */
  static <S, F, E> Validator<S, E> validatingField(@NonNull Function<? super S, ? extends F> getter,
                                                   @NonNull Validator<? super F, ? extends E> validator) {
    //noinspection unchecked
    return subject -> new ValidationContainer<>(subject, (List<E>) validator.validate(getter.apply(subject)).getErrors());
  }
}
