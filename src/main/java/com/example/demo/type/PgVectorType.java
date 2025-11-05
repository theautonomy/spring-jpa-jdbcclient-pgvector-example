package com.example.demo.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import com.pgvector.PGvector;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

/**
 * Custom Hibernate UserType for mapping PostgreSQL vector type to float[] in Java.
 *
 * <p>This UserType integrates the pgvector-java library with Hibernate, allowing seamless
 * conversion between PostgreSQL's vector type and Java's float array.
 */
public class PgVectorType implements UserType<float[]> {

    @Override
    public int getSqlType() {
        return Types.OTHER; // Use OTHER for custom types
    }

    @Override
    public Class<float[]> returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(float[] x, float[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(float[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public float[] nullSafeGet(
            ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        Object obj = rs.getObject(position);
        if (obj == null) {
            return null;
        }

        // PostgreSQL returns PGobject, we need to convert it to PGvector
        if (obj instanceof PGobject) {
            PGobject pgObj = (PGobject) obj;
            PGvector pgVector = new PGvector(pgObj.getValue());
            return pgVector.toArray();
        } else if (obj instanceof PGvector) {
            return ((PGvector) obj).toArray();
        }

        throw new IllegalArgumentException("Unexpected type for vector: " + obj.getClass());
    }

    @Override
    public void nullSafeSet(
            PreparedStatement st,
            float[] value,
            int index,
            SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, new PGvector(value));
        }
    }

    @Override
    public float[] deepCopy(float[] value) {
        return (value != null) ? Arrays.copyOf(value, value.length) : null;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(float[] value) {
        return deepCopy(value);
    }

    @Override
    public float[] assemble(Serializable cached, Object owner) {
        return (float[]) cached;
    }

    @Override
    public float[] replace(float[] detached, float[] managed, Object owner) {
        return deepCopy(detached);
    }
}
