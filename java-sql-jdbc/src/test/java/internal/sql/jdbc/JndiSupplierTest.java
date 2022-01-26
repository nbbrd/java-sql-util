/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.sql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.stream.Stream;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import nbbrd.sql.jdbc.InMemoryDriver;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;;

/**
 *
 * @author Philippe Charles
 */
public class JndiSupplierTest {

    @Test
    public void testGetConnection() {
        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> new JndiSupplier(() -> {
            throw new Exception("error");
        }).getConnection(InMemoryDriver.HSQLDB.name()))
                .withMessageContaining("Cannot retrieve context");

        JndiSupplier x = new JndiSupplier(InMemoryDriverContext::new);

        assertThatNullPointerException()
                .isThrownBy(() -> x.getConnection(null));

        assertThatExceptionOfType(SQLException.class)
                .isThrownBy(() -> x.getConnection(""));

        assertThatCode(() -> {
            try (Connection conn = x.getConnection(InMemoryDriver.HSQLDB.name())) {
            }
        }).doesNotThrowAnyException();
    }

    private static final class InMemoryDriverContext implements Context {

        @Override
        public Object lookup(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Object lookup(String name) throws NamingException {
            return Stream.of(InMemoryDriver.values())
                    .filter(driver -> driver.name().equals(name))
                    .map(driver -> DataSourceBasedSupplierTest.newDataSource(driver.getUrl()))
                    .findFirst()
                    .orElseThrow(NamingException::new);
        }

        @Override
        public void bind(Name name, Object obj) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void bind(String name, Object obj) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void rebind(Name name, Object obj) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void rebind(String name, Object obj) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void unbind(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void unbind(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void rename(Name oldName, Name newName) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void rename(String oldName, String newName) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void destroySubcontext(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void destroySubcontext(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Context createSubcontext(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Context createSubcontext(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Object lookupLink(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Object lookupLink(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public NameParser getNameParser(Name name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public NameParser getNameParser(String name) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Name composeName(Name name, Name prefix) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public String composeName(String name, String prefix) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Object addToEnvironment(String propName, Object propVal) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Object removeFromEnvironment(String propName) throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public Hashtable<?, ?> getEnvironment() throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public void close() throws NamingException {
            throw new NamingException("Not supported yet.");
        }

        @Override
        public String getNameInNamespace() throws NamingException {
            throw new NamingException("Not supported yet.");
        }
    }
}
