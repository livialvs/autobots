package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelos.EnderecoAtualizador;
import com.autobots.automanager.modelos.EnderecoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController
@RequestMapping("/enderecos")
public class EnderecoControle {

	@Autowired
	private ClienteRepositorio repositorio;
	
	@Autowired
	private EnderecoRepositorio repositorioEndereco;
	
	@Autowired
	private EnderecoSelecionador selecionadorEndereco;
	
	// get all
	@GetMapping
	public ResponseEntity<List<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorioEndereco.findAll();
		return enderecos.isEmpty() ?
                new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(enderecos, HttpStatus.OK);
	}
	
	// get by id
	@GetMapping("/{endId}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long endId) {
		Endereco endereco = selecionadorEndereco.selecionar(repositorioEndereco.findAll(), endId);
		return endereco == null ?
                new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(endereco, HttpStatus.OK);
	}
	
	// cadastro de endereco
	@PostMapping("/cadastro/{clienteId}")
	public ResponseEntity<Void> cadastrarEndereco(@PathVariable long clienteId, @RequestBody Endereco endereco) {
		Cliente cliente = repositorio.findById(clienteId).orElse(null);
		if (cliente != null) {
			cliente.setEndereco(endereco);
			repositorioEndereco.save(endereco);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		
	}
	
	// atualizar endereco
	@PutMapping("/atualizar/{endId}")
	public ResponseEntity<Void> atualizarEndereco(@PathVariable long endId, @RequestBody Endereco atualizacao) {
		Endereco endereco = repositorioEndereco.findById(endId).orElse(null);
		if (endereco != null) {
			new EnderecoAtualizador().atualizar(endereco, atualizacao);
			repositorioEndereco.save(endereco);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// deletar endereco
	@DeleteMapping("/deletar/{clienteId}")
	public ResponseEntity<Void> deletarEndereco(@PathVariable long clienteId) {
		Cliente cliente = repositorio.findById(clienteId).orElse(null);
		if (cliente != null && cliente.getEndereco() != null) {
			Endereco endereco = cliente.getEndereco();
			cliente.setEndereco(null);
			repositorio.save(cliente);
			repositorioEndereco.delete(endereco);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
