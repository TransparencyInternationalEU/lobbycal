package eu.transparency.lobbycal.domain;

import java.time.ZonedDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-10T21:42:32.171+0100")
@StaticMetamodel(Meeting.class)
public class Meeting_ {
	public static volatile SingularAttribute<Meeting, Long> id;
	public static volatile SingularAttribute<Meeting, String> title;
	public static volatile SingularAttribute<Meeting, String> submitter;
	public static volatile SingularAttribute<Meeting, String> aliasUsed;
	public static volatile SingularAttribute<Meeting, ZonedDateTime> startDate;
	public static volatile SingularAttribute<Meeting, ZonedDateTime> endDate;
	public static volatile SingularAttribute<Meeting, ZonedDateTime> createdDate;
	public static volatile SingularAttribute<Meeting, String> uid;
	public static volatile SetAttribute<Meeting, Tag> tags;
	public static volatile SetAttribute<Meeting, Partner> partners;
	public static volatile SingularAttribute<Meeting, User> user;
	public static volatile SingularAttribute<Meeting, String> mTag;
	public static volatile SingularAttribute<Meeting, String> mPartner;
}
