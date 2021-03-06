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

import java.util.NoSuchElementException;

import lombok.Value;

/**
 * @author pacien
 */
@Value class Success<R, E> implements Attempt<R, E> {
  R result;

  @Override public boolean isSuccess() {
    return true;
  }

  @Override public boolean isFailure() {
    return false;
  }

  @Override public E getError() {
    throw new NoSuchElementException();
  }
}
