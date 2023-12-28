package com.smart.dao;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	//pagination...
	
	
	@Query("from Contact as c where c.user.id =:userId")
	//pageable object contain data of:
	//currentPage-page
	//Contact Per page-(5)
	public Page<Contact> findContactByUser(@Param("userId")int userId, Pageable pePageable);

	//for searching the contact by name
	//search conatact which contain the letter same as per input
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
