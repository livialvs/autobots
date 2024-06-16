package com.autobots.automanager.controles;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import com.autobots.automanager.adicionadores.AdicionadorLinkVenda;
import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Mercadoria;
import com.autobots.automanager.entitades.Servico;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Veiculo;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.selecionadores.EmpresaSelecionador;
import com.autobots.automanager.selecionadores.VendaSelecionador;
import com.autobots.automanager.servicos.EmpresaServico;
import com.autobots.automanager.servicos.UsuarioServico;
import com.autobots.automanager.servicos.VeiculoServico;
import com.autobots.automanager.servicos.VendaServico;

@RestController
@RequestMapping("/vendas")
public class VendaControle {

    @Autowired
    private VendaServico servico;

    @Autowired
    private VendaSelecionador selecionador;

    @Autowired
    private UsuarioServico servicoUsuario;

    @Autowired
    private VeiculoServico servicoVeiculo;

    @Autowired
    private EmpresaServico servicoEmpresa;

    @Autowired
    private EmpresaSelecionador selecionadorEmpresa;

    @Autowired
    private AdicionadorLinkVenda adicionadorLinkVenda;

    @GetMapping
    public ResponseEntity<List<Venda>> buscarVendas() {
        List<Venda> vendas = servico.buscarVendas();
        if (vendas.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
        	adicionadorLinkVenda.adicionarLink(vendas);
            return ResponseEntity.ok(vendas);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarVenda(@PathVariable Long id) {
        Venda venda = servico.buscarVenda(id);
        if (venda == null) {
            return ResponseEntity.notFound().build();
        } else {
        	adicionadorLinkVenda.adicionarLink(venda);
            return ResponseEntity.ok(venda);
        }
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarVenda(@PathVariable Long id, @RequestBody Venda vendaAtualizada) {
        Venda vendaAtual = servico.buscarVenda(id);
        if (vendaAtual == null) {
            return ResponseEntity.notFound().build();
        } else {
            vendaAtualizada.setId(id);
            servico.salvarVenda(vendaAtualizada);
            return ResponseEntity.ok("Venda atualizada com sucesso");
        }
    }

    @PostMapping("/cadastro/{idEmpresa}")
    public ResponseEntity<?> cadastroVenda(
      @RequestBody Venda vendas,
      @PathVariable Long idEmpresa
    ) {
      List<Empresa> selecionarEmpresa = servicoEmpresa.buscarEmpresas();
      Empresa selecionada = selecionadorEmpresa.selecionar(
        selecionarEmpresa,
        idEmpresa
      );
      if (selecionada != null) {
        Usuario clienteSelecionado = servicoUsuario.buscarUsuario(
          vendas.getCliente().getId()
        );
        Usuario funcionarioSelecionado = servicoUsuario.buscarUsuario(
          vendas.getFuncionario().getId()
        );
        Veiculo veiculoSelecionador = servicoVeiculo.buscarVeiculo(
          vendas.getVeiculo().getId()
        );
        for (Mercadoria bodyMercadoria : vendas.getMercadorias()) {
          vendas.getMercadorias().clear();
          Mercadoria novaMercadoria = new Mercadoria();
          novaMercadoria.setDescricao(bodyMercadoria.getDescricao());
          novaMercadoria.setCadastro(bodyMercadoria.getCadastro());
          novaMercadoria.setFabricao(bodyMercadoria.getFabricao());
          novaMercadoria.setNome(bodyMercadoria.getNome());
          novaMercadoria.setQuantidade(bodyMercadoria.getQuantidade());
          novaMercadoria.setValidade(bodyMercadoria.getValidade());
          novaMercadoria.setValor(bodyMercadoria.getValor());
          vendas.getMercadorias().add(novaMercadoria);
        }
        for (Servico bodyServico : vendas.getServicos()) {
          Servico novoServico = new Servico();
          novoServico.setDescricao(bodyServico.getDescricao());
          novoServico.setNome(bodyServico.getNome());
          novoServico.setValor(bodyServico.getValor());
          vendas.getServicos().add(novoServico);
        }
        funcionarioSelecionado.getVendas().add(vendas);
        vendas.setCliente(clienteSelecionado);
        vendas.setFuncionario(funcionarioSelecionado);
        vendas.setVeiculo(veiculoSelecionador);
        selecionada.getVendas().add(vendas);
        servicoEmpresa.salvarEmpresa(selecionada);
        return new ResponseEntity<>(
          "Serviço cadastrado na empresa: " + selecionada.getNomeFantasia(),
          HttpStatus.CREATED
        );
      } else {
        return new ResponseEntity<>(
          "Empresa não encontrada",
          HttpStatus.NOT_FOUND
        );
      }
    }


    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarVendas(@PathVariable Long id) {
      List<Empresa> empresas = servicoEmpresa.buscarEmpresas();
      List<Veiculo> veiculos = servicoVeiculo.buscarVeiculos();
      List<Usuario> usuarios = servicoUsuario.buscarUsuarios();
      for (Empresa mercadoriaEmpresa : empresas) {
        for (Venda empresaMercadoria : mercadoriaEmpresa.getVendas()) {
          if (empresaMercadoria.getId() == id) {
            servicoEmpresa.deletarVenda(mercadoriaEmpresa.getId(), id);
          }
        }
      }
      for (Veiculo mercadoriaEmpresa : veiculos) {
        for (Venda empresaMercadoria : mercadoriaEmpresa.getVendas()) {
          if (empresaMercadoria.getId() == id) {
          	empresaMercadoria.setVeiculo(null);
            servicoVeiculo.deletarVenda(mercadoriaEmpresa.getId(), id);
          }
        }
      }
      for (Usuario mercadoriaEmpresa : usuarios) {
        for (Venda empresaMercadoria : mercadoriaEmpresa.getVendas()) {
          if (empresaMercadoria.getId() == id) {
            servicoUsuario.deletarVenda(mercadoriaEmpresa.getId(), id);
          }
        }
      }
      servico.deletarVenda(id);
      return null;
    }
}
