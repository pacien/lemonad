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

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author pacien
 */
class AttemptTest {
  @Test void testSimpleSuccess() {
    var result = "result";
    var success = Attempt.success(result);
    assertFalse(success.isFailure());
    assertTrue(success.isSuccess());
    assertThrows(NoSuchElementException.class, success::getError);
    assertEquals(result, success.getResult());
    success.ifFailure(__ -> fail());
    success.ifSuccess(innerResult -> assertEquals(result, innerResult));
  }

  @Test void testSimpleFailure() {
    var fault = 0;
    var failure = Attempt.failure(fault);
    assertTrue(failure.isFailure());
    assertFalse(failure.isSuccess());
    assertEquals(fault, failure.getError());
    assertThrows(NoSuchElementException.class, failure::getResult);
    failure.ifFailure(innerFault -> assertEquals(fault, innerFault));
    failure.ifSuccess(__ -> fail());
  }

  @Test void testNormalAttempt() {
    var result = "result";
    var success = Attempt.attempt(() -> result);
    assertFalse(success.isFailure());
    assertTrue(success.isSuccess());
    assertThrows(NoSuchElementException.class, success::getError);
    assertEquals(result, success.getResult());
    success.ifFailure(__ -> fail());
    success.ifSuccess(innerResult -> assertEquals(result, innerResult));
  }

  @Test void testFailedAttempt() {
    var exception = new Exception();
    var failure = Attempt.attempt(() -> {
      throw exception;
    });
    assertTrue(failure.isFailure());
    assertFalse(failure.isSuccess());
    assertEquals(exception, failure.getError());
    assertThrows(NoSuchElementException.class, failure::getResult);
    failure.ifFailure(innerFault -> assertEquals(exception, innerFault));
    failure.ifSuccess(__ -> fail());
  }

  @Test void testTransformationFlow() {
    var result0 = 0;
    var result1 = "res";
    var result2 = 0L;
    var fault0 = 0;
    var fault1 = 1;
    var fault2 = 2;

    Attempt.success(result0)
           .mapFailure(__ -> fail())
           .mapResult(res -> Attempt.success(result1))
           .mapResult(res -> {
             assertEquals(result1, res);
             return Attempt.failure(fault0);
           })
           .ifSuccess(__ -> fail())
           .mapResult(__ -> fail())
           .mapFailure(f -> {
             assertEquals(fault0, f);
             return Attempt.failure(fault1);
           })
           .mapFailure(f -> {
             assertEquals(fault1, f);
             return Attempt.success(result2);
           })
           .ifFailure(__ -> fail())
           .flatMap(attempt -> {
             assertEquals(result2, attempt.getResult());
             return Attempt.failure(fault2);
           })
           .ifSuccess(__ -> fail())
           .ifFailure(f -> assertEquals(fault2, f));
  }
}
