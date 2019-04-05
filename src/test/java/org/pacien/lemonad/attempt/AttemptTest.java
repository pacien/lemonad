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
    var error = 0;
    var failure = Attempt.failure(error);
    assertTrue(failure.isFailure());
    assertFalse(failure.isSuccess());
    assertEquals(error, failure.getError());
    assertThrows(NoSuchElementException.class, failure::getResult);
    failure.ifFailure(innerError -> assertEquals(error, innerError));
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
    failure.ifFailure(innerError -> assertEquals(exception, innerError));
    failure.ifSuccess(__ -> fail());
  }

  @Test void testTransformationFlow() {
    var result0 = 0;
    var result1 = "res";
    var result2 = 0L;
    var result3 = "0";
    var error0 = 0;
    var error1 = 0L;
    var error2 = "fail";
    var error3 = false;

    Attempt.<Integer, Long>success(result0)
      .mapError((Long err) -> {
        fail();
        return Attempt.failure(err);
      })
      .mapResult((Integer res) -> Attempt.success(result1))
      .mapResult((String res) -> {
        assertEquals(result1, res);
        return Attempt.<String, Integer>failure(error0);
      }, (Integer err) -> {
        assertEquals(error0, err);
        return (long) err;
      })
      .ifSuccess((String __) -> fail())
      .mapResult((String res) -> {
        fail();
        return Attempt.success(res);
      })
      .mapError((Long err) -> {
        assertEquals(error0, err);
        return Attempt.failure(error1);
      })
      .mapError((Long err) -> {
        assertEquals(error1, err);
        return Attempt.<Long, Long>success(result2);
      }, (Long res) -> {
        assertEquals(result2, res);
        return res.toString();
      })
      .ifFailure((Long err) -> fail())
      .flatMap((Attempt<? super String, ? super Long> attempt) -> {
        assertEquals(Long.toString(result2), attempt.getResult());
        return Attempt.<String, String>failure(error2);
      })
      .ifSuccess(__ -> fail())
      .ifFailure(f -> assertEquals(error2, f))
      .map((String __) -> Attempt.failure(error3), (String __) -> Attempt.success(result3))
      .ifSuccess((String result) -> assertEquals(result3, result))
      .ifFailure((Boolean __) -> fail());
  }
}
