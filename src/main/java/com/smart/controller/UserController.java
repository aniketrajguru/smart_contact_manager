package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;



@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		
		//get the user using username(Email)
		
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER "+user);
		
		model.addAttribute("user",user);
		
	}
	
	
	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)
	{
		
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open addcontact from handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading file..
		
		if(file.isEmpty())
		{
			//if the file is empty then try our message
			System.out.println("file is empty");
			//for default photo if img=null
			contact.setImage("default_img.png");
		}
		else {
			//file the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
		
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded..");
		}
		
		user.getContact().add(contact);
		
		contact.setUser(user);
		
		this.userRepository.save(user);
		
		System.out.println("DATA "+contact);
		
		System.out.println("Added to data base");
		
		//message success after adding contact
		session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));
		
		} catch (Exception e) {
			
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			
			//message error
			session.setAttribute("message", new Message("Something went wrong !! Try again", "danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page = 5(n)
	//current page = 0 [page]
	@GetMapping("show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");
		//send contact list from database
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//currentPage-page
		//Contact Per page-(5)
		Pageable pageable = PageRequest.of(page, 8);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing specific contact details.
	@RequestMapping("/{cId}/contact")
	//cid recieve with help of @PathVariable
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) 
	{
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//security bug
		//if statement use for provide security to user 
		// user1 login then they able to access data of user2 with manually trying cid in url
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId())
		{	
			model.addAttribute("contact", contact);
			model.addAttribute("title",contact.getName());
		}
		return "normal/contact_detail";
	}
	
	//delete contact handler..
	//HttpSession is use to send msg ex-succesfully deleted
	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session, Principal principal)
	{
		System.out.println("CID "+cId);
		 
		Contact contact = this.contactRepository.findById(cId).get();
		
		//with help of contactOptional get the contact
		//Contact contact = contactOptional.get();
		
		System.out.println("Contact "+contact.getcId());
		
		//contact.setUser(null);
		
		User user = this.userRepository.getUserByUserName(principal.getName());

		user.getContact().remove(contact);
		
		this.userRepository.save(user);
		
		
		System.out.println("DELETE");
		
		session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m)
	{
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact" ,contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	//@ModelAttribute use to store which entity/model data are comming
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m
			,HttpSession session
			,Principal principal) {
			
		
		try {
			
			//fetch old contact details
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			
			//image...
			if(!file.isEmpty())
			{
				//file work update new file
				//rewrite 
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();
				
				
				
				//update new photo
				
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			
			session.setAttribute("message", new Message("Contact updated successfully..", "sucess"));
			
			
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT ID "+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	//Your profile handle
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
}
 