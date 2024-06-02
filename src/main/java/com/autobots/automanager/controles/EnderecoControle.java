package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController
public class EnderecoControle {

	@Autowired
	private ClienteRepositorio repositorio;
	
	@Autowired
	private EnderecoRepositorio repositorioEndereco;
	
	@GetMapping("/endereco")
	public List<Endereco> buscarEnderecos(){
		List<Endereco> endereco = repositorioEndereco.findAll();
		return endereco;
	}
	
	@PostMapping("/cadastrar/endereco/{id}")
	public void cadastrarEndereco(@RequestBody Endereco endereco, @PathVariable long id) {
		Cliente alvo = repositorio.getById(id);
		alvo.setEndereco(endereco);
		repositorioEndereco.save(endereco);
	}
	
	@PutMapping("/atualizar/endereco")
	public void atualizarEndereco(
		@RequestBody Endereco atualizacao) {
		Endereco end = repositorioEndereco.getById(atualizacao.getId());
		EnderecoAtualizador atualizador = new EnderecoAtualizador();
		atualizador.atualizar(end, atualizacao);
		repositorioEndereco.save(end);
	}
	
	
}
