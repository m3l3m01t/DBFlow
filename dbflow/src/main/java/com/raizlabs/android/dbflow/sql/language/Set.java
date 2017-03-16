package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Used to specify the SET part of an {@link com.raizlabs.android.dbflow.sql.language.Update} query.
 */
public class Set<TModel> extends BaseTransformable<TModel> implements WhereBase<TModel> {

    private OperatorGroup operatorGroup;

    private Query update;

    public Set(Query update, Class<TModel> table) {
        super(table);
        this.update = update;
        operatorGroup = new OperatorGroup();
        operatorGroup.setAllCommaSeparated(true);
    }

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    @NonNull
    public Set<TModel> conditions(SQLOperator... conditions) {
        operatorGroup.andAll(conditions);
        return this;
    }

    @NonNull
    public Set<TModel> conditionValues(ContentValues contentValues) {
        SqlUtils.addContentValues(contentValues, operatorGroup);
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder =
            new QueryBuilder(update.getQuery())
                .append("SET ")
                .append(operatorGroup.getQuery()).appendSpace();
        return queryBuilder.getQuery();
    }

    /**
     * @return A {@link Transaction.Builder} handle to begin an async transaction.
     * A simple helper method.
     */
    public Transaction.Builder asyncTransaction() {
        return FlowManager.getDatabaseForTable(getTable())
            .beginTransactionAsync(new ITransaction() {
                @Override
                public void execute(DatabaseWrapper databaseWrapper) {
                    Set.this.executeUpdateDelete(databaseWrapper);
                }
            });
    }

    @NonNull
    @Override
    public Query getQueryBuilderBase() {
        return update;
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return BaseModel.Action.UPDATE;
    }
}
