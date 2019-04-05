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

package org.pacien.lemonad.attempt;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.NonNull;

/**
 * Wraps either a value from a success or an error from a failure.
 *
 * @param <R> the potential wrapped result type.
 * @param <E> the potential error type.
 * @author pacien
 */
public interface Attempt<R, E> {
  /**
   * @return whether the {@link Attempt} is successful.
   */
  boolean isSuccess();

  /**
   * @return whether the {@link Attempt} is failed.
   */
  boolean isFailure();

  /**
   * @return the result if this {@link Attempt} is a success.
   * @throws java.util.NoSuchElementException if this {@link Attempt} is a failure.
   */
  R getResult();

  /**
   * @return the error if this {@link Attempt} is a failure.
   * @throws java.util.NoSuchElementException if this {@link Attempt} is a success.
   */
  E getError();

  /**
   * @param resultConsumer a {@link Consumer} of result called if the {@link Attempt} is a success.
   * @return the current {@link Attempt}.
   */
  default Attempt<R, E> ifSuccess(@NonNull Consumer<? super R> resultConsumer) {
    if (isSuccess()) resultConsumer.accept(getResult());
    return this;
  }

  /**
   * @param errorConsumer a {@link Consumer} of error called if the {@link Attempt} is a failure.
   * @return the current {@link Attempt}.
   */
  default Attempt<R, E> ifFailure(@NonNull Consumer<? super E> errorConsumer) {
    if (isFailure()) errorConsumer.accept(getError());
    return this;
  }

  /**
   * @param transformer a function producing an {@link Attempt}, called with the current result if this {@link Attempt} is a success.
   * @return this {@link Attempt} if it is a failure, or the produced one otherwise.
   */
  default <RR> Attempt<RR, E> transformResult(@NonNull Function<? super R, ? extends Attempt<? extends RR, ? extends E>> transformer) {
    //noinspection unchecked
    return (Attempt<RR, E>) (isSuccess() ? transformer.apply(getResult()) : this);
  }

  /**
   * @param transformer  a function producing an {@link Attempt}, called with the current result if this {@link Attempt} is a success.
   * @param errorAdapter a function adapting any intermediate error returned by the {@code transformer} function.
   * @return this {@link Attempt} if it is a failure, or the produced one otherwise.
   */
  default <RR, IE> Attempt<RR, E> transformResult(@NonNull Function<? super R, ? extends Attempt<? extends RR, ? extends IE>> transformer,
                                                  @NonNull Function<? super IE, ? extends E> errorAdapter) {
    return transformResult(transformer.andThen(attempt -> attempt.recoverError(errorAdapter.andThen(Attempt::failure))));
  }

  /**
   * @param mapper a function mapping used to map any result if this {@link Attempt} is a success.
   * @return this {@link Attempt} if it is a failure, or the mutated one otherwise.
   */
  default <RR> Attempt<RR, E> mapResult(@NonNull Function<? super R, ? extends RR> mapper) {
    return transformResult(mapper.andThen(Attempt::success));
  }

  /**
   * @param recoverer a function producing an {@link Attempt}, called with the current error if this {@link Attempt} is a failure.
   * @return this {@link Attempt} if it is a success, or the alternative {@link Attempt} retrieved from the supplier otherwise.
   */
  default <EE> Attempt<R, EE> recoverError(@NonNull Function<? super E, ? extends Attempt<? extends R, ? extends EE>> recoverer) {
    //noinspection unchecked
    return (Attempt<R, EE>) (isFailure() ? recoverer.apply(getError()) : this);
  }

  /**
   * @param recoverer     a function producing an {@link Attempt}, called with the current error if this {@link Attempt} is a failure.
   * @param resultAdapter a function adapting any intermediate result returned by the {@code recoverer} function.
   * @return this {@link Attempt} if it is a success, or the alternative {@link Attempt} retrieved from the supplier otherwise.
   */
  default <IR, EE> Attempt<R, EE> recoverError(@NonNull Function<? super E, ? extends Attempt<? extends IR, ? extends EE>> recoverer,
                                               @NonNull Function<? super IR, ? extends R> resultAdapter) {
    return recoverError(recoverer.andThen(attempt -> attempt.transformResult(resultAdapter.andThen(Attempt::success))));
  }

  /**
   * @param mapper a function mapping used to map any result if this {@link Attempt} is a failure.
   * @return this {@link Attempt} if it is a success, or the mutated one otherwise.
   */
  default <EE> Attempt<R, EE> mapError(@NonNull Function<? super E, ? extends EE> mapper) {
    return recoverError(mapper.andThen(Attempt::failure));
  }

  /**
   * @param resultTransformer a function producing an {@link Attempt}, called with the current result if this {@link Attempt} is a success.
   * @param errorTransformer  a function producing an {@link Attempt}, called with the current error if this {@link Attempt} is a failure.
   * @return the transformed {@link Attempt}.
   */
  default <RR, EE> Attempt<RR, EE> transform(@NonNull Function<? super R, ? extends Attempt<? extends RR, ? extends EE>> resultTransformer,
                                             @NonNull Function<? super E, ? extends Attempt<? extends RR, ? extends EE>> errorTransformer) {
    //noinspection unchecked
    return (Attempt<RR, EE>) (isSuccess() ? resultTransformer.apply(getResult()) : errorTransformer.apply(getError()));
  }

  /**
   * @param mapper a function transforming an {@link Attempt}.
   * @return the transformed {@link Attempt}.
   */
  default <RR, EE> Attempt<RR, EE> flatMap(@NonNull Function<? super Attempt<? super R, ? super E>, ? extends Attempt<? extends RR, ? extends EE>> mapper) {
    //noinspection unchecked
    return (Attempt<RR, EE>) mapper.apply(this);
  }

  /**
   * @param result the result of the {@link Attempt}.
   * @return a successful {@link Attempt} wrapping the supplied result.
   */
  static <R, E> Attempt<R, E> success(R result) {
    return new Success<>(result);
  }

  /**
   * @param error the cause of the failure of the {@link Attempt}.
   * @return a failed {@link Attempt} with the supplied error.
   */
  static <R, E> Attempt<R, E> failure(E error) {
    return new Failure<>(error);
  }

  /**
   * @param supplier a {@code Supplier} that may throw an {@link Throwable}.
   * @return an {@link Attempt} wrapping either the result of the execution of the supplier or any thrown {@link Throwable}.
   */
  static <R, E extends Throwable> Attempt<R, E> attempt(@NonNull ThrowingSupplier<? extends R, ? extends E> supplier) {
    try {
      return success(supplier.get());
    } catch (Throwable throwable) {
      //noinspection unchecked
      return (Attempt<R, E>) failure(throwable);
    }
  }
}
