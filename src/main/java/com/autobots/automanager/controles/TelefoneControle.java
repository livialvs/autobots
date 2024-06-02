package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@RestController
public class TelefoneControle {
	
	@Autowired
	private ClienteRepositorio repositorio;
	
	@Autowired
	private TelefoneRepositorio repositorioTelefone;
	
	@GetMapping("/telefone")
	public List<Telefone> telefone(){
		return repositorioTelefone.findAll();
	}
	
	@PutMapping("/cadastrar/telefone")
	public void cadastrarTelefone(@RequestBody Cliente atualizacao) {
		Cliente alvo = repositorio.getById(atualizacao.getId());
		alvo.getTelefones().addAll(atualizacao.getTelefones());
		repositorio.save(alvo);
	}
	
	@PutMapping("/atualizar/telefone/{id}")
	public void atualizarTelefone(@PathVariable long id, @RequestBody Telefone atualizacao){
		Telefone telefone = repositorioTelefone.getById(atualizacao.getId());
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(telefone, atualizacao);
		repositorioTelefone.save(telefone);
	}
	
	@DeleteMapping("/deletar/telefone/{id}")
	public void deletarTelefone(@RequestBody Telefone delecao, @PathVariable long id){
	Telefone alvo = repositorioTelefone.getById(delecao.getId());
	Cliente cliente = repositorio.getById(id);
	cliente.getTelefones().remove(alvo);
	repositorioTelefone.delete(alvo);
	}
}
