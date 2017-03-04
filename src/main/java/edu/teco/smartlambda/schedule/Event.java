package edu.teco.smartlambda.schedule;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.lambda.Lambda;
import lombok.Getter;
import lombok.Setter;
import org.quartz.CronExpression;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Melanie on 01.02.2017.
 */
@Entity
@Table(name = "ScheduleEvent")
public class Event {
	@Getter
	@Setter
	private String   cronExpression;
	@Getter
	@Setter
	private Calendar nextExecution;
	@Getter
	@Setter
	private String   name;
	@Getter
	@Setter
	private String   parameters;
	@Getter
	@Setter
	private Calendar lock;
	@Getter
	@Setter
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	private Key      key;
	@Getter
	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambda")
	private Lambda   lambda;
	
	public void execute() {
		try {
			this.nextExecution.setTime(new CronExpression(this.cronExpression).getNextValidTimeAfter(new Date()));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		this.lock = Calendar.getInstance();
		lambda.executeAsync(parameters);
	}
	
	public void save() {
		try {
			this.nextExecution.setTime(new CronExpression(this.cronExpression).getNextValidTimeAfter(new Date()));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
}
