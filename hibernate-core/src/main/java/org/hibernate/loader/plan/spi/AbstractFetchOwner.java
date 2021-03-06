/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.loader.plan.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.plan.internal.LoadPlanBuildingHelper;
import org.hibernate.loader.plan.spi.build.LoadPlanBuildingContext;
import org.hibernate.persister.walking.spi.AssociationAttributeDefinition;
import org.hibernate.persister.walking.spi.CompositionDefinition;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 * @author Gail Badner
 */
public abstract class AbstractFetchOwner extends AbstractPlanNode implements FetchOwner {

	private List<Fetch> fetches;

	public AbstractFetchOwner(SessionFactoryImplementor factory) {
		super( factory );
		validate();
	}

	private void validate() {
	}

	/**
	 * A "copy" constructor.  Used while making clones/copies of this.
	 *
	 * @param original - the original object to copy.
	 */
	protected AbstractFetchOwner(AbstractFetchOwner original, CopyContext copyContext) {
		super( original );
		validate();

		copyContext.getReturnGraphVisitationStrategy().startingFetches( original );
		if ( fetches == null || fetches.size() == 0 ) {
			this.fetches = Collections.emptyList();
		}
		else {
			// TODO: don't think this is correct...
			List<Fetch> fetchesCopy = new ArrayList<Fetch>();
			for ( Fetch fetch : fetches ) {
				fetchesCopy.add( fetch.makeCopy( copyContext, this ) );
			}
			this.fetches = fetchesCopy;
		}
		copyContext.getReturnGraphVisitationStrategy().finishingFetches( original );
	}

	public void addFetch(Fetch fetch) {
		if ( fetch.getOwner() != this ) {
			throw new IllegalArgumentException( "Fetch and owner did not match" );
		}

		if ( fetches == null ) {
			fetches = new ArrayList<Fetch>();
		}

		fetches.add( fetch );
	}

	@Override
	public Fetch[] getFetches() {
		return fetches == null ? NO_FETCHES : fetches.toArray( new Fetch[ fetches.size() ] );
	}

	protected abstract FetchOwnerDelegate getFetchOwnerDelegate();

	@Override
	public boolean isNullable(Fetch fetch) {
		return getFetchOwnerDelegate().isNullable( fetch );
	}

	@Override
	public Type getType(Fetch fetch) {
		return getFetchOwnerDelegate().getType( fetch );
	}

	@Override
	public String[] getColumnNames(Fetch fetch) {
		return getFetchOwnerDelegate().getColumnNames( fetch );
	}

	@Override
	public CollectionFetch buildCollectionFetch(
			AssociationAttributeDefinition attributeDefinition,
			FetchStrategy fetchStrategy,
			LoadPlanBuildingContext loadPlanBuildingContext) {
		return LoadPlanBuildingHelper.buildStandardCollectionFetch(
				this,
				attributeDefinition,
				fetchStrategy,
				loadPlanBuildingContext
		);
	}

	@Override
	public EntityFetch buildEntityFetch(
			AssociationAttributeDefinition attributeDefinition,
			FetchStrategy fetchStrategy,
			LoadPlanBuildingContext loadPlanBuildingContext) {
		return LoadPlanBuildingHelper.buildStandardEntityFetch(
				this,
				attributeDefinition,
				fetchStrategy,
				loadPlanBuildingContext
		);
	}

	@Override
	public CompositeFetch buildCompositeFetch(
			CompositionDefinition attributeDefinition,
			LoadPlanBuildingContext loadPlanBuildingContext) {
		return LoadPlanBuildingHelper.buildStandardCompositeFetch( this, attributeDefinition, loadPlanBuildingContext );
	}

}
