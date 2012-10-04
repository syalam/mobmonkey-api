package com.MobMonkey.Resources;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.MobMonkey.Models.Location;
import com.MobMonkey.Models.Status;
import com.MobMonkey.Models.Trending;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;

@Path("/trending")
public class TrendingResource extends ResourceHelper {
	static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	public TrendingResource(){
		super();
	}
	
	@Path("/bookmarks")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBookmarksInJSON(@QueryParam("timeSpan") String timeSpan)
	{
		if(timeSpan == null){
			return Response.status(500).entity(new Status("Failure", "Need to provide query parameter \'timeSpan\'. Valid params are day, week, month","")).build();
		}
		int timeSpanInDays = 0;
		if(timeSpan.toLowerCase().equals("day")){
			timeSpanInDays = 1;
		}else if(timeSpan.toLowerCase().equals("week")){
			timeSpanInDays = 7;
		}else if(timeSpan.toLowerCase().equals("month")){
			timeSpanInDays = 31;
		}
		long oneDay = 24L * 60L * 60L * 1000L; 
		Date now = new Date();
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	    String timeSpanString = dateFormatter.format(now.getTime() - oneDay * timeSpanInDays);
	    
	    DynamoDBQueryExpression scanExpression = new DynamoDBQueryExpression(new AttributeValue().withS("Bookmark"));
	    scanExpression.setRangeKeyCondition(
	    		new Condition().withComparisonOperator(ComparisonOperator.GT)
	    		.withAttributeValueList(new AttributeValue().withS(timeSpanString)));
	    
	    HashMap<String, Integer> popularity = new HashMap<String, Integer>();
		PaginatedQueryList<Trending> trends = super.mapper().query(Trending.class, scanExpression);
	    
		for(Trending trend : trends){
			String key = trend.getLocationId() + ":" + trend.getProviderId();
			if(!popularity.containsKey(key)){
				popularity.put(key, 1);
			}else{
				popularity.put(key, popularity.get(key) + 1);
			}
		
		}
		List<Location> sortedList = new ArrayList<Location>();
		for (Entry<String, Integer> entry  : entriesSortedByValues(popularity)) {
			Location loc = new Location();
			String[] locprov = entry.getKey().split(":");
			loc.setProviderId(locprov[1]);
			loc.setLocationId(locprov[0]);
		   sortedList.add(loc);
		}
		return Response.ok().entity(sortedList).build();
		
	}
	
	@Path("/interests")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInterestsInJSON(){
		return null;
		
	}
	
	@Path("/topviewed")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopViewedInJSON()
	{
		return null;
		
	}
	
	@Path("/nearme")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNearMeInJSON(){
		return null;
		
	}
	
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
