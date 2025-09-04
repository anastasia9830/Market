package de.tub;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor

/*
  Autorisierte Nutzerinnen (bspw. Angestellte der Bo ̈rse) ko ̈nnen Gu ̈ter hinzufu ̈gen.
  только авторизированные пользователи (например работники биржи) могут добавлять товары
 */

public class Authorized_Users { // can be record class
        private final String login;
        private final String password;
        private final String role; // admin or seller
    }

